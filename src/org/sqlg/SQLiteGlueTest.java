package org.sqlg;

import android.app.Activity;
import android.os.Bundle;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.sqlg.SQLiteGlue;

import net.sqlc.*;

import java.io.File;

import java.sql.SQLException;

public class SQLiteGlueTest extends Activity
{
  ArrayAdapter<String> resultsAdapter;

  int errorCount = 0;

  /* package */ void logErrorItem(String result) {
    android.util.Log.e("SQLiteGlueTest", result);
    resultsAdapter.add(result);
  }

  /* package */ void checkIntegerResult(String label, int actual, int expected) {
    if (expected == actual) {
      logResult(label + " - OK");
    } else {
      ++errorCount;
      logErrorItem("FAILED CHECK" + label);
      logErrorItem("expected: " + expected);
      logErrorItem("actual: " + actual);
    }
  }

  /* package */ void checkLongResult(String label, long actual, long expected) {
    if (expected == actual) {
      logResult(label + " - OK");
    } else {
      ++errorCount;
      logErrorItem("FAILED CHECK" + label);
      logErrorItem("expected: " + expected);
      logErrorItem("actual: " + actual);
    }
  }

  /* package */ void checkDoubleResult(String label, double actual, double expected) {
    if (expected == actual) {
      logResult(label + " - OK");
    } else {
      ++errorCount;
      logErrorItem("FAILED CHECK" + label);
      logErrorItem("expected: " + expected);
      logErrorItem("actual: " + actual);
    }
  }

  /* package */ void checkStringResult(String label, String actual, String expected) {
    if (expected.equals(actual)) {
      logResult(label + " - OK");
    } else {
      ++errorCount;
      logErrorItem("FAILED CHECK" + label);
      logErrorItem("expected: " + expected);
      logErrorItem("actual: " + actual);
    }
  }

  /* package */ void logResult(String result) {
    android.util.Log.i("SQLiteGlueTest", result);
    resultsAdapter.add(result);
  }

  /* package */ void logError(String result) {
    logErrorItem(result);
    ++errorCount;
  }

  /* package */ void logUnexpectedException(String result, java.lang.Exception ex) {
    android.util.Log.e("SQLiteGlueTest", "UNEXPECTED EXCEPTION IN " + result, ex);
    resultsAdapter.add("UNEXPECTED EXCEPTION IN " + result + " : " + ex);
    ++errorCount;
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    ArrayAdapter<String> r1 =
      resultsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
    ListView lv1 = (ListView)findViewById(R.id.results);
    lv1.setAdapter(r1);

  //  try {
  //    runTest();
  //  } catch (java.sql.SQLException ex) {
  //    android.util.Log.w("SQLiteGlueTest", "unexpected sql exception", ex);
  //    r1.add("unexpected sql exception" + ex);
  //    return;
  //  } catch (java.lang.Exception ex) {
  //    android.util.Log.w("SQLiteGlueTest", "unexpected exception", ex);
  //    r1.add("unexpected exception: " + ex);
  //    return;
  //  }
  //}

  ///* package */ void runTest() {
    try {

    // load library:
    SQLiteConnector connector = new SQLiteGlueConnector();

    File dbfile = new File(getFilesDir(), "lltest.db");

    SQLDatabaseHandle mydb = new SQLGDatabaseHandle(dbfile.getAbsolutePath(),
        SQLiteOpenFlags.READWRITE | SQLiteOpenFlags.CREATE);

    int rc = mydb.open();

    if (rc != SQLCode.OK) {
        logError("open error: " + rc);
        return;
    }

    rc = mydb.close();

    checkIntegerResult("first close rc", rc, 0);

    mydb = new SQLGDatabaseHandle(dbfile.getAbsolutePath(), SQLiteOpenFlags.READWRITE);
    rc = mydb.open();
    if (rc != SQLCode.OK) {
      logError("second DB open error: " + rc);
      return;
    }

    rc = mydb.close();

    checkIntegerResult("second close rc", rc, 0);

    mydb = new SQLGDatabaseHandle(dbfile.getAbsolutePath(), SQLiteOpenFlags.READWRITE);
    rc = mydb.open();
    if (rc != SQLCode.OK) {
      logError("third DB open error: " + rc);
      return;
    }

    //sthandle = SQLiteGlue.sqlg_db_prepare_st(dbhandle, "SELECT UPPER('How about some ascii text?') AS caps");
    SQLStatementHandle sthandle = mydb.newStatementHandle("SELECT UPPER('How about some ascii text?') AS caps");
    rc = sthandle.prepare();

    if (rc != SQLCode.OK) {
      logError("prepare statement error: " + rc);
      mydb.close();
      return;
    }

    sthandle.step();

    int colcount1 = sthandle.getColumnCount();
    checkIntegerResult("SELECT UPPER() column count: ", colcount1, 1);

/*
    if (colcount1 > 0) {
      String colname = SQLiteGlue.sqlg_st_column_name(sthandle, 0);
      checkStringResult("SELECT UPPER() caps column name", colname, "caps");

      int coltype = SQLiteGlue.sqlg_st_column_type(sthandle, 0);
      checkIntegerResult("SELECT UPPER() caps column type", coltype, 3);

      String coltext = SQLiteGlue.sqlg_st_column_text_native(sthandle, 0);
      checkStringResult("SELECT UPPER() as caps", coltext, "HOW ABOUT SOME ASCII TEXT?");
    }
    */

    rc = sthandle.finish();

    if (rc != SQLCode.OK) {
      logError("prepare statement error: " + rc);
      mydb.close();
      return;
    }

    rc = mydb.close();

    checkIntegerResult("third close rc", rc, 0);



/*
    SQLiteConnector connector = new SQLiteGlueConnector();

    File dbfile = new File(getFilesDir(), "mytest.db");

    SQLiteConnection mydbc = null;

    try {
      mydbc = connector.newSQLiteConnection(dbfile.getAbsolutePath(),
        SQLiteOpenFlags.READWRITE | SQLiteOpenFlags.CREATE);
    } catch (SQLException ex) {
      logUnexpectedException("DB open exception", ex);
      return;
    }

    mydbc.dispose();

    // try to reopen database:
    try {
      mydbc = connector.newSQLiteConnection(dbfile.getAbsolutePath(), SQLiteOpenFlags.READWRITE);
    } catch (SQLException ex) {
      logUnexpectedException("DB open exception", ex);
      return;
    }

    SQLiteStatement st;

    try {
      st = mydbc.prepareStatement("select upper('How about some ascii text?') as caps");
    } catch (SQLException ex) {
      logUnexpectedException("prepare statement exception", ex);
      mydbc.dispose();
      return;
    }

    st.step();

    int colcount = st.getColumnCount();
    r1.add(new String("column count: " + colcount));
    android.util.Log.i("SQLiteGlueTest", "column count: " + colcount);

    String colname = st.getColumnName(0);
    r1.add(new String("column name: " + colname));
    android.util.Log.i("SQLiteGlueTest", "column name: " + colname);

    int coltype = st.getColumnType(0);
    android.util.Log.i("SQLiteGlueTest", "column type: " + coltype);

    String first = st.getColumnTextNativeString(0);

    r1.add(new String("upper: " + first));

    st.dispose();

    mydbc.dispose();

    // try to reopen database:
    try {
      mydbc = connector.newSQLiteConnection(dbfile.getAbsolutePath(), SQLiteOpenFlags.READWRITE);
    } catch (SQLException ex) {
      logUnexpectedException("DB open exception", ex);
      return;
    }

    try {
      st = mydbc.prepareStatement("drop table if exists tt;");
    } catch (SQLException ex) {
      logUnexpectedException("prepare statement exception", ex);
      mydbc.dispose();
      return;
    }
    st.step();
    st.dispose();

    try {
      st = mydbc.prepareStatement("create table if not exists tt (text1 text, num1 integer, num2 integer, real1 real)");
    } catch (SQLException ex) {
      logUnexpectedException("prepare statement exception", ex);
      mydbc.dispose();
      return;
    }
    st.step();
    st.dispose();

    /* XXX Brody TODO:
    // test statement error handling (seems to throw exception here):
    try {
      // seems to fail here:
      st = mydbc.prepareStatement("INSERT INTO tt (data, data_num) VALUES (?,?)");

      st.step();
      st.dispose();

      // should not get here:
      android.util.Log.w("SQLiteGlueTest", "ERROR: statement should not have succeeded");
      r1.add("ERROR: statement should not have succeeded");
    } catch (SQLException ex) {
      android.util.Log.w("SQLiteGlueTest", "prepare statement exception, as expected OK", ex);
      r1.add("prepare statement exception, as expected OK: " + ex);
      // TODO dispose statement??
    }
    // * /

    try {
      st = mydbc.prepareStatement("INSERT INTO tt (text1, num1, num2, real1) VALUES (?,?,?,?)");

      // should not get here:
    } catch (SQLException ex) {
      logUnexpectedException("prepare statement exception (not expected)", ex);
      mydbc.dispose();
      return;
    }
    st.bindTextNativeString(1, "test");
    st.bindInteger(2, 10100);
    st.bindLong(3, 0x1230000abcdL);
    st.bindDouble(4, 123456.789);

    boolean sr = st.step();
    while (sr) {
      android.util.Log.i("SQLiteGlueTest", "step next");
      sr = st.step();
    }
    android.util.Log.i("SQLiteGlueTest", "last step " + sr);
    st.dispose();

    try {
      st = mydbc.prepareStatement("select * from tt;");
    } catch (SQLException ex) {
      logUnexpectedException("prepare statement exception", ex);
      mydbc.dispose();
      return;
    }

    sr = st.step();
    while (sr) {
      android.util.Log.i("SQLiteGlueTest", "step next");
      r1.add("step next");

      colcount = st.getColumnCount();
      android.util.Log.i("SQLiteGlueTest", "column count: " + colcount);

      for (int i=0;i<colcount;++i) {
        colname = st.getColumnName(i);
        android.util.Log.i("SQLiteGlueTest", "column " + i + " name: " + colname);
        r1.add("column " + i + " name: " + colname);

        coltype = st.getColumnType(i);
        android.util.Log.i("SQLiteGlueTest", "column " + i + " type: " + coltype);
        r1.add("column " + i + " type: " + coltype);

        String text = st.getColumnTextNativeString(i);
        android.util.Log.i("SQLiteGlueTest", "col " + i + " text " + text);
        r1.add("col " + i + " text " + text);
      }

      sr = st.step();
    }
    android.util.Log.i("SQLiteGlueTest", "last step " + sr);
    r1.add("last step " + sr);

    st.dispose();

    // XXX TODO fails with SQL error code 5 (SQLITE_BUSY):
    //mydbc.dispose();

    /* XXX TODO:
    // try to reopen database:
    try {
      mydbc = connector.newSQLiteConnection(dbfile.getAbsolutePath(),
        SQLiteOpenFlags.READWRITE | SQLiteOpenFlags.CREATE);
    } catch (SQLException ex) {
      android.util.Log.w("SQLiteGlueTest", "DB open exception", ex);
      return;
    }

    // try to cleanup the table:
    try {
      st = mydbc.prepareStatement("drop table if exists tt;");
    } catch (SQLException ex) {
      android.util.Log.w("SQLiteGlueTest", "prepare statement exception", ex);
      mydbc.dispose();
      return;
    }
    st.step();
    st.dispose();

    mydbc.dispose();
    */

    } catch (java.lang.Exception ex) {
      logUnexpectedException("unexpected exception", ex);
      return;
    }

  }
}
