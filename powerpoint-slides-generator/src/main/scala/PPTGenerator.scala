
import javax.xml.bind._

import org.docx4j.openpackaging.io._
import org.docx4j.openpackaging.packages._
import org.docx4j.openpackaging.parts.WordprocessingML._
import org.docx4j.wml._

import scalaz._
import Scalaz._
import scala.xml._
import scala.xml.parsing.ConstructingParser._
import scala.io.Source.fromFile


import org.apache.log4j.Logger
import org.docx4j.XmlUtils
import org.pptx4j.jaxb.Context
import org.docx4j.openpackaging.packages.PresentationMLPackage
import org.docx4j.openpackaging.packages.PresentationMLPackage._
import org.docx4j.openpackaging.parts.PartName
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart
import org.docx4j.openpackaging.parts.PresentationML.SlidePart
import org.pptx4j.pml.Shape

//import org.pptx4j.jaxb._

object OpenSQLITemplate {
  val mindMap : Elem =  fromSource(fromFile("Q:\\PERSO_GIT_REPO_CLONE\\formations\\W-JSC\\src\\main\\webapp\\Formation javascript W-JSC.mm"), false).document.docElem.asInstanceOf[Elem]

  val firstSlideMainTitleXMLContentTemplate = fromFile("./src/main/scala/slide_title_mainTitle_fragment.xml").getLines.mkString
  val firstSlideSecondTitleXMLContentTemplate = fromFile("./src/main/scala/slide_title_subTitle_fragment.xml").getLines.mkString

  val slideContentMainTemplate = fromFile("./src/main/scala/slide_content_main_fragment.xml").getLines.mkString
  val slideContent1STMainTemplate = fromFile("./src/main/scala/slide_content_main_level1_fragment.xml").getLines.mkString
  val slideContent2NDSTMainTemplate = fromFile("./src/main/scala/slide_content_main_level2_fragment.xml").getLines.mkString

  def loadSqliPPTTemplate : PresentationMLPackage = {
    OpcPackage.load(new java.io.File("editTemplate_PPT_Formation_Charte_Groupe_2012_v1.0.potx")).asInstanceOf[PresentationMLPackage]
  }

  def getSQLILayoutsFromPPT(presentationMLPackage : PresentationMLPackage) : Seq[SlideLayoutPart] = {
    //1 first slide
    //2 text slide
    Seq(1,2).map(idx => presentationMLPackage.getParts.getParts.get(new PartName("/ppt/slideLayouts/slideLayout"+idx+".xml")).asInstanceOf[SlideLayoutPart])
  }

  def createNewPPTTemplate : PresentationMLPackage = {
    // Create skeletal package, including a MainPresentationPart and a SlideLayoutPart
    PresentationMLPackage.createPackage().asInstanceOf[PresentationMLPackage]
  }

  def unmarshal(str:String) : Shape = {
    XmlUtils.unmarshalString(str, Context.jcPML).asInstanceOf[Shape]
  }

  def addPresentationFirstSlide(pp : MainPresentationPart, layout : SlideLayoutPart) {
    val slide = createSlidePart(pp,layout,new PartName("/ppt/slides/slide6.xml")) // in testing time, should be 1 in final version !
    slide.getJaxbElement.getCSld.getSpTree.getSpOrGrpSpOrGraphicFrame.add(unmarshal(
        firstSlideMainTitleXMLContentTemplate.replace("@@Titre@@", ""+(mindMap \ "node").head \ "@TEXT")
      ))
    slide.getJaxbElement.getCSld.getSpTree.getSpOrGrpSpOrGraphicFrame.add(unmarshal(firstSlideSecondTitleXMLContentTemplate))
  }

  def addContentSlide(pp : MainPresentationPart, layout : SlideLayoutPart) {
    val slide = createSlidePart(pp,layout,new PartName("/ppt/slides/slide7.xml")) // in testing time, should be computed in final version !

    var levels = slideContent1STMainTemplate.replace("@@VAL@@",""+(mindMap \ "node" \ "node").tail.tail.head \ "@TEXT")
    val secondLvlTitle = ((mindMap \ "node" \ "node").tail.tail.head \ "node").map( _ \ "@TEXT")
    for( title <- secondLvlTitle){
      levels = levels + slideContent2NDSTMainTemplate.replace("@@VAL@@",""+title)
    }
    println("Levels are " + levels)
    println("results is " + slideContentMainTemplate.replace("@@LEVELS@@", levels))

    // todo recursive this !
    slide.getJaxbElement.getCSld.getSpTree.getSpOrGrpSpOrGraphicFrame.add(unmarshal(
        slideContentMainTemplate.replace("@@LEVELS@@", levels)
      ))
  }

  def run() {
    val presentationMLPackage = loadSqliPPTTemplate
    val pp = presentationMLPackage.getParts.getParts.get(new PartName("/ppt/presentation.xml")).asInstanceOf[MainPresentationPart];

    val layouts = getSQLILayoutsFromPPT(presentationMLPackage)

    addPresentationFirstSlide(pp,layouts.head)
    addContentSlide(pp,layouts.tail.head)

    presentationMLPackage.save(new java.io.File("c:\\hop.pptx"));
  }

  val SAMPLE_SHAPE =
    "<p:sp xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">"+ "<p:nvSpPr>"+ "<p:cNvPr id=\"4\" name=\"Title 3\" />"+ "<p:cNvSpPr>"+ "<a:spLocks noGrp=\"1\" />"+ "</p:cNvSpPr>"+ "<p:nvPr>"+ "<p:ph type=\"title\" />"+ "</p:nvPr>"+ "</p:nvSpPr>"+ "<p:spPr />"+ "<p:txBody>"+ "<a:bodyPr />"+ "<a:lstStyle />"+ "<a:p>"+ "<a:r>"+ "<a:rPr lang=\"en-US\" smtClean=\"0\" />"+ "<a:t>Hello World</a:t>"+ "</a:r>"+ "<a:endParaRPr lang=\"en-US\" />"+ "</a:p>"+ "</p:txBody>"+ "</p:sp>"

}