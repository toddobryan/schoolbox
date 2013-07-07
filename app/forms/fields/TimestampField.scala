package forms.fields

import forms.validators.ValidationError
import java.sql.Timestamp
import forms.widgets.TimestampInput

abstract class BaseTimestampField[T](name: String)(implicit man: Manifest[T]) extends Field[T](name) {
	
  val uuid = java.util.UUID.randomUUID()
  
  override def widget = new TimestampInput(required, uuid=uuid)
}

class TimestampField[T](name: String) extends BaseTimestampField[Timestamp](name) {
  def asValue(s: Seq[String]): Either[ValidationError, Timestamp] =
    try {
      //Identical to DateField code 
      val format = new java.text.SimpleDateFormat("yyyy")
      val yearPrefix = format.format(java.util.Calendar.getInstance().getTime()).substring(0, 2)
      
      val splitdate = s(0).split("/")
      
      if(splitdate(0).length == 1) splitdate(0) = "0"+splitdate(0)
      if(splitdate(1).length == 1) splitdate(1) = "0"+splitdate(0)
      if(splitdate(2).length == 2) splitdate(2) = yearPrefix+splitdate(2)
      if(splitdate(2).length < 4) Left(ValidationError("Make sure you input a 4 digit year."))
      
      val date = java.sql.Date.valueOf(splitdate(2)+"-"+splitdate(0)+"-"+splitdate(1)).toString
      
      //Identical to TimeField code except s(1)
      val splitString = s(1).split(" ")
      val splitTime = splitString(0).split(":")
      splitString(1)=splitString(1).capitalize
      var hours = splitTime(0).toInt
      if (hours > 12) Left(ValidationError("Please make sure input is valid"))
      else if (splitTime(1).toInt > 59) Left(ValidationError("Please make sure input is valid"))
      else{
        if (hours == 12 && splitString(1) == "AM") hours = 0
        if (splitString(1) == "PM" && hours != 12) hours = hours + 12
        splitTime(0) = hours.toString
        val time = splitTime(0) + ":" + splitTime(1) + ":00"
        Right(Timestamp.valueOf(date + " " + time))
      }
    } catch {
      case e: IllegalArgumentException => Left(ValidationError("Please make sure input is valid"))
    }
}

class TimestampFieldOptional[T](name: String) extends BaseTimestampField[Option[Timestamp]](name) {
  def asValue(s: Seq[String]): Either[ValidationError, Option[Timestamp]] =
    s match {
    case Seq() => Right(None)
    case Seq(date,"") => Left(ValidationError("Time was not specified"))
    case Seq("", time) => Left(ValidationError("Date was not specified"))
    case Seq(date, time) => try {
      //Identical to DateField code 
      val format = new java.text.SimpleDateFormat("yyyy")
      val yearPrefix = format.format(java.util.Calendar.getInstance().getTime()).substring(0, 2)
      
      val splitdate = s(0).split("/")
      
      if(splitdate(0).length == 1) splitdate(0) = "0"+splitdate(0)
      if(splitdate(1).length == 1) splitdate(1) = "0"+splitdate(0)
      if(splitdate(2).length == 2) splitdate(2) = yearPrefix+splitdate(2)
      if(splitdate(2).length < 4) Left(ValidationError("Make sure you input a 4 digit year."))
      
      val date = java.sql.Date.valueOf(splitdate(2)+"-"+splitdate(0)+"-"+splitdate(1)).toString
      
      //Identical to TimeField code except s(1)
      val splitString = s(1).split(" ")
      val splitTime = splitString(0).split(":")
      var hours = splitTime(0).toInt
      if (hours > 12) Left(ValidationError("Please make sure input is valid"))
      else if (splitTime(1).toInt > 59) Left(ValidationError("Please make sure input is valid"))
      else{
        splitString(1)=splitString(1).capitalize
        if (hours == 12 && splitString(1) == "AM") hours = 0
        if (splitString(1) == "PM" && hours != 12) hours = hours + 12
        splitTime(0) = hours.toString
        val time = splitTime(0) + ":" + splitTime(1) + ":00"
        Right(Option(Timestamp.valueOf(date + " " + time)))
      }
    } catch {
      case e: IllegalArgumentException => Left(ValidationError("Please make sure input is a valid time"))
    }
      
    }
}