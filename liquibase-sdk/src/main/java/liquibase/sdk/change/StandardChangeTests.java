package liquibase.sdk.change;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.sdk.Context;
import liquibase.util.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class StandardChangeTests {

    private Set<Class> seenChangeClasses;
    private Context context;

    @Before
    public void setup() {
        context = Context.getInstance();
        seenChangeClasses = context.getSeenExtensionClasses().get(Change.class);
        if (seenChangeClasses == null) {
            seenChangeClasses = new HashSet<Class>();
        }
    }

    @Test
    public void allFoundClassesAreRegistered() {
        for (Class clazz : seenChangeClasses) {
            try {
                clazz.newInstance();
            } catch (Throwable e) {
                fail("Error instantiating " + clazz.getName() + ", extension classes need a public no-arg constructor: " + e.getMessage());
            }
        }
    }

    @Test
    public void atLeastOneSupportedDatabase() throws Exception {
        List<Database> databases = DatabaseFactory.getInstance().getImplementedDatabases();

        for (Class clazz : seenChangeClasses) {
            Change change = (Change) clazz.newInstance();
            for (Database database : databases) {
                if (change.supports(database)) {
                    return;
                }
            }
            fail("No databases supported change "+change.getClass().getName()+". Tried "+StringUtils.join(databases, ",", new StringUtils.StringUtilsFormatter() {
                @Override
                public String toString(Object obj) {
                    return ((Database) obj).getShortName();
                }
            }));
        }
    }

//    @Test
//    public void minimumRequiredIsValidSql() throws Exception {
//        ChangeFactory changeFactory = ChangeFactory.getInstance();
//        for (String changeName : changeFactory.getDefinedChanges()) {
//            if (changeName.equals("addDefaultValue")) {
//                continue; //need to better handle strange "one of defaultValue* is required" logic
//            }
//            if (changeName.equals("changeWithNestedTags") || changeName.equals("sampleChange")) {
//                continue; //not a real change
//            }
//            for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
//                if (database.getShortName() == null) {
//                    continue;
//                }
//
//                TestState state = new TestState(name.getMethodName(), changeName, database.getShortName(), TestState.Type.SQL);
//                state.addComment("Database: " + database.getShortName());
//
//                Change change = changeFactory.create(changeName);
//                if (!change.supports(database)) {
//                    continue;
//                }
//                if (change.generateStatementsVolatile(database)) {
//                    continue;
//                }
//                ChangeMetaData changeMetaData = ChangeFactory.getInstance().getChangeMetaData(change);
//
//                change.setResourceAccessor(new JUnitResourceAccessor());
//
//                for (String paramName : new TreeSet<String>(changeMetaData.getRequiredParameters(database).keySet())) {
//                    ChangeParameterMetaData param = changeMetaData.getParameters().get(paramName);
//                    Object paramValue = param.getExampleValue();
//                    String serializedValue;
//                    serializedValue = formatParameter(paramValue);
//                    state.addComment("Change Parameter: " + param.getParameterName() + "=" + serializedValue);
//                    param.setValue(change, paramValue);
//                }
//
//                ValidationErrors errors = change.validate(database);
//                assertFalse("Validation errors for " + changeMetaData.getName() + " on " + database.getShortName() + ": " + errors.toString(), errors.hasErrors());
//
//                SqlStatement[] sqlStatements = change.generateStatements(database);
//                for (SqlStatement statement : sqlStatements) {
//                    Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, database);
//                    if (sql == null) {
//                        System.out.println("Null sql for " + statement + " on " + database.getShortName());
//                    } else {
//                        for (Sql line : sql) {
//                            String sqlLine = line.toSql();
//                            assertFalse("Change "+changeMetaData.getName()+" contains 'null' for "+database.getShortName()+": "+sqlLine, sqlLine.contains(" null "));
//
//                            state.addValue(sqlLine + ";");
//                        }
//                    }
//                }
//                state.test();
//            }
//        }
//    }
//
//    @Test
//    public void lessThanMinimumFails() throws Exception {
//        ChangeFactory changeFactory = ChangeFactory.getInstance();
//        for (String changeName : changeFactory.getDefinedChanges()) {
//            for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
//                if (database.getShortName() == null) {
//                    continue;
//                }
//
//                Change change = changeFactory.create(changeName);
//                if (!change.supports(database)) {
//                    continue;
//                }
//                if (change.generateStatementsVolatile(database)) {
//                    continue;
//                }
//                ChangeMetaData changeMetaData = ChangeFactory.getInstance().getChangeMetaData(change);
//
//                change.setResourceAccessor(new JUnitResourceAccessor());
//
//                ArrayList<String> requiredParams = new ArrayList<String>(changeMetaData.getRequiredParameters(database).keySet());
//                for (String paramName : requiredParams) {
//                    ChangeParameterMetaData param = changeMetaData.getParameters().get(paramName);
//                    Object paramValue = param.getExampleValue();
//                    param.setValue(change, paramValue);
//                }
//
//                for (int i = 0; i < requiredParams.size(); i++) {
//                    String paramToRemove = requiredParams.get(i);
//                    ChangeParameterMetaData paramToRemoveMetadata = changeMetaData.getParameters().get(paramToRemove);
//                    Object currentValue = paramToRemoveMetadata.getCurrentValue(change);
//                    paramToRemoveMetadata.setValue(change, null);
//
//                    assertTrue("No errors even with "+changeMetaData.getName()+" with a null "+paramToRemove+" on "+database.getShortName(), change.validate(database).hasErrors());
//                    paramToRemoveMetadata.setValue(change, currentValue);
//                }
//            }
//        }
//    }
//
//    @Test
//    public void extraParamsIsValidSql() throws Exception {
//        ChangeFactory changeFactory = ChangeFactory.getInstance();
//        for (String changeName : changeFactory.getDefinedChanges()) {
//            if (changeName.equals("addDefaultValue")) {
//                continue; //need to better handle strange "one of defaultValue* is required" logic
//            }
//
//            for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
//                if (database.getShortName() == null) {
//                    continue;
//                }
//
//                TestState state = new TestState(name.getMethodName(), changeName, database.getShortName(), TestState.Type.SQL);
//                state.addComment("Database: " + database.getShortName());
//
//                Change baseChange = changeFactory.create(changeName);
//                if (!baseChange.supports(database)) {
//                    continue;
//                }
//                if (baseChange.generateStatementsVolatile(database)) {
//                    continue;
//                }
//                ChangeMetaData changeMetaData = ChangeFactory.getInstance().getChangeMetaData(baseChange);
//                ArrayList<String> optionalParameters = new ArrayList<String>(changeMetaData.getOptionalParameters(database).keySet());
//                Collections.sort(optionalParameters);
//
//                List<List<String>> paramLists = powerSet(optionalParameters);
//                Collections.sort(paramLists, new Comparator<List<String>>() {
//                    public int compare(List<String> o1, List<String> o2) {
//                        int comp = Integer.valueOf(o1.size()).compareTo(o2.size());
//                        if (comp == 0) {
//                            comp =  StringUtils.join(o1, ",").compareTo(StringUtils.join(o2, ","));
//                        }
//                        return comp;
//                    }
//                });
//                for (List<String> permutation : paramLists) {
//                    Change change = changeFactory.create(changeName);
//                    change.setResourceAccessor(new JUnitResourceAccessor());
////
//                    for (String paramName : new TreeSet<String>(changeMetaData.getRequiredParameters(database).keySet())) {
//                        ChangeParameterMetaData param = changeMetaData.getParameters().get(paramName);
//                        Object paramValue = param.getExampleValue();
//                        String serializedValue;
//                        serializedValue = formatParameter(paramValue);
//                        state.addComment("Required Change Parameter: "+ param.getParameterName()+"="+ serializedValue);
//                        param.setValue(change, paramValue);
//                    }
//
//                    for (String paramName : permutation) {
//                        ChangeParameterMetaData param = changeMetaData.getParameters().get(paramName);
//                        if (!param.supports(database)) {
//                            continue;
//                        }
//                        Object paramValue = param.getExampleValue();
//                        String serializedValue;
//                        serializedValue = formatParameter(paramValue);
//                        state.addComment("Optional Change Parameter: "+ param.getParameterName()+"="+ serializedValue);
//                        param.setValue(change, paramValue);
//
//                    }
//
//                    ValidationErrors errors = change.validate(database);
//                    assertFalse("Validation errors for " + changeMetaData.getName() + " on "+database.getShortName()+": " +errors.toString(), errors.hasErrors());
////
////                    SqlStatement[] sqlStatements = change.generateStatements(database);
////                    for (SqlStatement statement : sqlStatements) {
////                        Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, database);
////                        if (sql == null) {
////                            System.out.println("Null sql for "+statement+" on "+database.getShortName());
////                        } else {
////                            for (Sql line : sql) {
////                                state.addValue(line.toSql()+";");
////                            }
////                        }
////                    }
////                    state.test();
//                }
//            }
//        }
//    }
}