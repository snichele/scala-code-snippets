package fp

import java.io.FileWriter
import java.sql._
import scala.xml._
import scala.xml.parsing.ConstructingParser._
import scala.io.Source.fromFile
import org.springframework.jdbc.core._
import org.springframework.jdbc.datasource.DriverManagerDataSource

import scalaz._
import Scalaz._

import org.slf4j._


// ZipoExport.performExport("xxxx.jdbc.xxxxxDriver","jdbc:xxxx:xxxx:@xxxx:xxxx:xxxx","USER","PASSWORD",Set("1983919906","000000312","349742105"),"'20100601'","'20120101'",path_to_xml_requests_file)
object ZipoExportFP {
  val log = LoggerFactory.getLogger("log")

  def using[A <: {def close(): Unit}, B](param: A)(f: A => B): B = try { f(param) } finally { param.close() }

  def performExport(driverClassName : String, jdbcUrl : String, user : String, password : String, startDate : String, endDate : String, xmlFilePathWithRequests : String) = {
    val tpl = new JdbcTemplate(new DriverManagerDataSource(driverClassName,jdbcUrl,user,password))

    def loadedRequests : Map[String,String]= {
      (fromSource(fromFile(xmlFilePathWithRequests), false).document.docElem.asInstanceOf[Elem] \\ "entry").map( n => Tuple2( (n \\ "@key").head.text, n.text)).toMap
    }

    val replace = (from: String, to: String) => (_:String).replaceAll(from, to)
    val fillSqlParams =
      replace(":PARAM_DATE_ANALYSE_DEB",startDate) ∘ replace(":PARAM_DATE_ANALYSE_FIN",endDate) ∘ replace(":PARAM_DATE_ANALYSE",startDate)

    val requestsToRunIdsByDomain = Map('zipoTitre -> Seq("maturite","evolution","countries","instruments","positionRows","positionSummary"),
                            'zipoInvest -> Seq("DAT_E1","DAT_E2","DAT_E3","DAT_E4"))
    val requestsToRunByDomain = requestsToRunIdsByDomain.map(x=>(x._1,loadedRequests filterKeys requestsToRunIdsByDomain(x._1).toSet))

    val institutionsIdsFor = Map('zipoTitre -> Seq("1096906842","1103022446","1116453354","115402645","1158611680","1179845602","1254166320","1319166077","1352229862","1368003952","1522787177","1568966220","1601736425","1613480722","1657066188","1666431008","167466110","1970348588","2089212705","319764780","369213210","372743091","382711321","409700264","435516885","529259383","569447964","595676605","813825768","819458548","825115170","829587824","999663237"),
                                      'zipoInvest -> Set("1796559213","1588994493","1699551908","597106667","1601736425","2131591221","1158611680","790815334","392240952","1603477587","1842882669","160884728","1613480722","182087818","1666431008","68288661","334557345","1657066188","1145733997","167466110","824796091","1320101920","1179845602","545293143","819458548","224010825","1558432955","57175041","1116453354","478925283","1706945775","902073900","216078951","382711321","942123948","369213210","1714981261","319764780","2089212705","999663237","731289590","1917651000","1304739594","1873569492","1671900762","1670440832","115402645","194257633","475092059","1014323464","595676605","1709176069","1738103691","435516885","1734407636","1856266697","715935468","1350477229","1103022446","1936428867","1061314026","1096906842","543212637","827915050","1033129373","1544688135","266010142","569447964","1970348588","1450954618","409700264","1282506371","372743091","2005171852","2131680888","382835985","2037791435","227706745","1368003952","247481381","813825768","353010591"))
    //    val requestsInstitutionsPairsByDomain =  requestNamesByDomain.map(x=>(x._1,x._2 <|*|> institutionsIdsFor(x._1)))

    def getFileNameFor(domain:Symbol,name:String) : String = (domain.toString drop 1) + "_" + name + ".txt"

    case class FileToGenerateInfos(fileName : String, associatedRequestName : String)
    val filesToGenerate = requestsToRunIdsByDomain.map(entry=>
      (entry._1,
       entry._2.map(
          requestName=> FileToGenerateInfos(getFileNameFor(entry._1,requestName),requestName))
      )
    )

    def dumpToFile(request:String,institutionId:String)(dumpRow : (ResultSet,FileWriter)=>Unit)(implicit writer : FileWriter ) {
      val requestParametrized = request |> fillSqlParams ∘ replace(":PARAM_ID_CLIENT_ERMS",institutionId)

      tpl.execute(
        requestParametrized,
        new PreparedStatementCallback[Unit]() {
          override def doInPreparedStatement(ps : PreparedStatement) {
            def proceedWithNextResultSetRow(implicit resultSet: ResultSet, isFirstRow:Boolean) {
              if (resultSet.next){
                dumpRow(resultSet,writer)
                proceedWithNextResultSetRow(resultSet,false)
              }else{
                log.debug("end of datas...")
              }
            }
            Option(ps.executeQuery).map(proceedWithNextResultSetRow(_,true))
          }
        }
      )
    }

    filesToGenerate.map{ entry =>
      val domain = entry._1
      val filesInfos = entry._2
      filesInfos.map { fileInfos =>
        using (new FileWriter("c:\\"+fileInfos.fileName)) { implicit writer =>
          institutionsIdsFor(domain).map{ institutionId =>
            log.info(domain + " - Dumping request " + fileInfos.associatedRequestName + " for " + institutionId)
            dumpToFile(requestsToRunByDomain(domain)(fileInfos.associatedRequestName),institutionId){(rs: ResultSet,writer : FileWriter) =>
              for(idx <- 1 to rs.getMetaData.getColumnCount){
                writer.write(if(rs.getObject(idx) != null) rs.getObject(idx).toString else "")
                writer.write("\t")
              }
              writer.write("\n")
            }
          }
        }
      }
    }
  }
}