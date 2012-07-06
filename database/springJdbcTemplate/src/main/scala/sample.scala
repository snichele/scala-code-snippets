/*
  Prepared statment, and spring jdbc template snippets
*/

object SimpleJDBC {
  def run() = {
    import java.sql._
    Class.forName ("oracle.jdbc.OracleDriver");
    val conn = DriverManager.getConnection("jdbc:oracle:thin:@hidv109.unix-int.intra-int.bdf-int.local:1521:DERMS", "ERMS_IDMI", "p4$$word");
    val p = conn.prepareStatement("SELECT INSTITUTION_ZIPO_ID_SEQUENCE.NEXTVAL FROM DUAL");
    val rs = p.executeQuery
    Option(p.executeQuery).filter(_.next).map(_.getString(1))
  }
}

object JdbcTemplateSnippet {
  def run() = {
    import org.springframework.jdbc.core.JdbcTemplate
    import org.springframework.jdbc.datasource.DriverManagerDataSource
    import java.sql.PreparedStatement
    import org.springframework.jdbc.core.PreparedStatementCallback

    val tpl = 
      new JdbcTemplate(
        new DriverManagerDataSource(
          "oracle.jdbc.OracleDriver",
          "jdbc:oracle:thin:@hidv109.unix-int.intra-int.bdf-int.local:1521:DERMS", 
          "ERMS_IDMI", 
          "p4$$word"
        )
      )

    def getNextId : String = {
      tpl.execute(
        "SELECT INSTITUTION_ZIPO_ID_SEQUENCE.NEXTVAL FROM DUAL",
        new PreparedStatementCallback[String]() {
          override def doInPreparedStatement(ps : PreparedStatement) : String = {
            Option(ps.executeQuery).filter(_.next).map(_.getString(1)).get
          }
        }
      )
    }

    getNextId
  }
}