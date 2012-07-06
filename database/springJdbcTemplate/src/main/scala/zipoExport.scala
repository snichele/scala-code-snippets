/* Zipo export */
import java.io.FileWriter
import java.sql._
import scala.xml._
import scala.xml.parsing.ConstructingParser._
import scala.io.Source.fromFile
import org.springframework.jdbc.core._
import org.springframework.jdbc.datasource.DriverManagerDataSource

// ZipoExport.performExport("xxxx.jdbc.xxxxxDriver","jdbc:xxxx:xxxx:@xxxx:xxxx:xxxx","USER","PASSWORD",Set("1983919906","000000312","349742105"),"'20100601'","'20120101'",path_to_xml_requests_file)
object ZipoExport {
  def using[A <: {def close(): Unit}, B](param: A)(f: A => B): B = try { f(param) } finally { param.close() }

  def performExport(driverClassName : String, jdbcUrl : String, user : String, password : String, institutions : Set[String], datInstitutions : Set[String],startDate : String, endDate : String, xmlFilePathWithRequests : String) = {

    val tpl = new JdbcTemplate(new DriverManagerDataSource(driverClassName,jdbcUrl,user,password))

    def loadRequests : Map[String,String]= {
      (fromSource(fromFile(xmlFilePathWithRequests), false).document.docElem.asInstanceOf[Elem] \\ "entry").map( n => Tuple2( (n \\ "@key").head.text, n.text)).toMap
    }
    val sqlRequestByDomain = Map('zipoTitre -> Seq("maturite","evolution","countries","instruments","positionRows","positionSummary","DAT_E1","DAT_E2","DAT_E3","DAT_E4"),
                                 'zipoInvest -> Seq("DAT_E1","DAT_E2","DAT_E3","DAT_E4"))

    val requestToRunIds = Seq("maturite","evolution","countries","instruments","positionRows","positionSummary","DAT_E1","DAT_E2","DAT_E3","DAT_E4")
    val datRequestToRunIds = Seq()
    val requestsToRun  = loadRequests filterKeys requestToRunIds.toSet
    val datRequestsToRun  = loadRequests filterKeys datRequestToRunIds.toSet
    val colCountByRequest = Seq(5,5,8,12,12,7)
    val colsCountByRequestPairs = requestToRunIds.zip(colCountByRequest)

    val importsDef =  "import java.util.List;import com.google.common.collect.Lists;import lombok.Data;import lombok.AllArgsConstructor;"
    def colsDef(nbr:Int) : String = (1 to nbr).map("private String col"+_+";").mkString
    def beanClassDef(request:String, colCountByRequest:Int) : String = { "@Data @AllArgsConstructor public class "+request.capitalize+" { "+colsDef(colCountByRequest) + " public List<"+request.capitalize+"> getDatas(){"}
    def beanListDef(request:String) : String = { "return Lists.newArrayList("}
    def beanAddToListStart(request:String) : String = { "new "+request.capitalize+"(" }
    val beanListEnd = "));}}"

    val beanClassDefByRequest = colsCountByRequestPairs.map(p=>(p._1,beanClassDef(p._1,p._2))).toMap;
    val beansListDefByRequest = requestToRunIds.map(r=> (r,beanListDef(r))).toMap

    val beanAddToListStartByRequest = requestToRunIds.map(r=> (r,beanAddToListStart(r))).toMap
    val beanInstanceAddEndSeparatorDef = "), //"
    val beanInstanceAddLastEndSeparatorDef = "),"
    val now = java.util.Calendar.getInstance().getTime()

    def ?%?(str : String)(implicit fileWriter : FileWriter)  { %?("\n" + str + "\n") }
    def %?(str : String = "")(implicit fileWriter : FileWriter) { %(str + "\n") }
    def %(str : String)(implicit fileWriter : FileWriter) { fileWriter.write(str) }

    for(r <- requestsToRun){
      using (new FileWriter("c:\\"+r._1+".txt")) {implicit fileWriter =>

        val replacements = Map(":PARAM_DATE_ANALYSE_DEB" -> startDate,":PARAM_DATE_ANALYSE_FIN" -> endDate,":PARAM_DATE_ANALYSE" -> startDate)
//        %?("// Raw zipo export (" + now +")")

//        ?%?("// ** Executing " + r._1)

//        %?(importsDef)
//        %?(beanClassDefByRequest(r._1))
//        %?(beansListDefByRequest(r._1))

        for(i <- institutions) {
//          ?%?("// -- Institution " + i )
          val request = r._2.replaceAll(":PARAM_DATE_ANALYSE_DEB",startDate).replaceAll(":PARAM_DATE_ANALYSE_FIN",endDate).replaceAll(":PARAM_DATE_ANALYSE",startDate).replaceAll(":PARAM_ID_CLIENT_ERMS",i)
          tpl.execute(
            request,
            new PreparedStatementCallback[Unit]() {
              override def doInPreparedStatement(ps : PreparedStatement) {
                def dumpNextResultSetRow(resultSet: ResultSet, isFirstRow:Boolean) {

                  if (resultSet.next){
//                    if(!isFirstRow)%(beanInstanceAddEndSeparatorDef)
                    if(isFirstRow && i == institutions.head) {
                      for(idx <- 1 to resultSet.getMetaData.getColumnCount){
                        %(resultSet.getMetaData.getColumnName(idx) + "\t")
                      }
                      %("\n")
                    }
//                    ?
//                    %(beanAddToListStartByRequest(r._1))
                    for(idx <- 1 to resultSet.getMetaData.getColumnCount){
                      if(resultSet.getObject(idx) != null) {
//                        %("\""+resultSet.getObject(idx).toString + "\"")
                        %(resultSet.getObject(idx).toString + "\t")
                      }else{
//                        %("\"\"")
                        %("\t")
                      }
//                      if(idx < resultSet.getMetaData.getColumnCount) %(",");
                    }
                    %("\n")
                    dumpNextResultSetRow(resultSet,false)
                  }else{
//                    if(i != institutions.last) %(beanInstanceAddLastEndSeparatorDef)
                  }
                }
                Option(ps.executeQuery).map(dumpNextResultSetRow(_,true))
              }
            }
          )
        }

//        %?(beanListEnd)
      }
    }
  }
}