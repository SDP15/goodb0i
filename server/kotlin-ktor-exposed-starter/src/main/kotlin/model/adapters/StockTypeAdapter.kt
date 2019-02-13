package model.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import model.Stock

object StockTypeAdapter : TypeAdapter<Stock>() {
    override fun write(out: JsonWriter, value: Stock) {
        out.beginObject()
        out.name("id")
        out.value(value.id.value.toString())
        out.name("name")
        out.value(value.name)
        out.name("superDepartment")
        out.value(value.superDepartment)
        out.name("ContentsMeasureType")
        out.value(value.contentsMeasureType)
        out.name("UnitOfSale")
        out.value(value.unitOfSale)
        out.name("description")
        out.beginArray()
        value.description.split("//").forEach {
            out.value(it)
        }
        out.endArray()
        out.name("AverageSellingUnitWeight")
        out.value(value.averageSellingUnitWeight)
        out.name("UnitQuantity")
        out.value(value.unitQuantity)
        out.name("contentsQuantity")
        out.value(value.contentsQuantity)
        out.name("department")
        out.value(value.department)
        out.name("price")
        out.value(value.price)
        out.name("unitPrice")
        out.value(value.unitPrice)
        out.endObject()
    }

    override fun read(`in`: JsonReader?): Stock {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
