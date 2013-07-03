package models.books

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import util.PersistableFile

import scalajdo.DataStore

@PersistenceCapable(detachable="true")
class PurchaseGroup {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id

  @Persistent
  private[this] var _title: Title = _
  def title: Title = _title
  def title_=(theTitle: Title) { _title = theTitle }

  @Persistent
  private[this] var _purchaseDate: java.sql.Date = _
  def purchaseDate: java.sql.Date = _purchaseDate
  def purchaseDate_=(thePurchaseDate: java.sql.Date) { _purchaseDate = thePurchaseDate }

  private[this] var _price: Double = _
  def price: Double = _price
  def price_=(thePrice: Double) { _price = thePrice }

  def this(title: Title, purchaseDate: java.sql.Date, price: Double) = {
    this()
    _title = title
    _purchaseDate = purchaseDate
    _price = price
  }

  override def toString: String = {
    "Purchased %s: copies of %s at $%.2f each".format(
        purchaseDate, title.name, price)
  }

  def numCopies(): Int = {
    val copyCand = QCopy.candidate
    DataStore.pm.query[Copy].filter(copyCand.isLost.eq(false).and(
        copyCand.purchaseGroup.eq(this))).executeList().length
  }

  def numLost(): Int = {
    val copyCand = QCopy.candidate
    DataStore.pm.query[Copy].filter(copyCand.isLost.eq(true).and(
        copyCand.purchaseGroup.eq(this))).executeList().length
  }
}

object PurchaseGroup {
  def getById(id: Long): Option[PurchaseGroup] = {
    val cand = QPurchaseGroup.candidate
    DataStore.pm.query[PurchaseGroup].filter(cand.id.eq(id)).executeOption()
  }
}

trait QPurchaseGroup extends PersistableExpression[PurchaseGroup] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _title: ObjectExpression[Title] = new ObjectExpressionImpl[Title](this, "_title")
  def title: ObjectExpression[Title] = _title

  private[this] lazy val _purchaseDate: ObjectExpression[java.sql.Date] = new ObjectExpressionImpl[java.sql.Date](this, "_purchaseDate")
  def purchaseDate: ObjectExpression[java.sql.Date] = _purchaseDate

  private[this] lazy val _price: NumericExpression[Double] = new NumericExpressionImpl[Double](this, "_price")
  def price: NumericExpression[Double] = _price
}

object QPurchaseGroup {
  def apply(parent: PersistableExpression[PurchaseGroup], name: String, depth: Int): QPurchaseGroup = {
    new PersistableExpressionImpl[PurchaseGroup](parent, name) with QPurchaseGroup
  }

  def apply(cls: Class[PurchaseGroup], name: String, exprType: ExpressionType): QPurchaseGroup = {
    new PersistableExpressionImpl[PurchaseGroup](cls, name, exprType) with QPurchaseGroup
  }

  private[this] lazy val jdoCandidate: QPurchaseGroup = candidate("this")

  def candidate(name: String): QPurchaseGroup = QPurchaseGroup(null, name, 5)

  def candidate(): QPurchaseGroup = jdoCandidate

  def parameter(name: String): QPurchaseGroup = QPurchaseGroup(classOf[PurchaseGroup], name, ExpressionType.PARAMETER)

  def variable(name: String): QPurchaseGroup = QPurchaseGroup(classOf[PurchaseGroup], name, ExpressionType.VARIABLE)
}
