package converter

import java.util.regex.Pattern

enum class UnitType(val allowsNegatives: Boolean = false) {
    LENGTH,
    WEIGHT,
    TEMPERATURE(true)
}

enum class Units(val type: UnitType, private val singular: String, val plural: String, val pattern: Pattern, val toBase: (Double) -> Double, val fromBase: (Double) -> Double) {
    M(UnitType.LENGTH, "meter"),
    KM(UnitType.LENGTH, "kilometer", 1000.0),
    CM(UnitType.LENGTH, "centimeter", 0.01),
    MM(UnitType.LENGTH, "millimeter", 0.001),
    MI(UnitType.LENGTH, "mile", 1609.35),
    YD(UnitType.LENGTH, "yard", 0.9144),
    FT(UnitType.LENGTH, "foot", 0.3048, "feet"),
    IN(UnitType.LENGTH, "inch", 0.0254, "inches"),

    G(UnitType.WEIGHT, "gram"),
    KG(UnitType.WEIGHT, "kilogram", 1000.0),
    MG(UnitType.WEIGHT, "milligram", 0.001),
    LB(UnitType.WEIGHT, "pound", 453.592),
    OZ(UnitType.WEIGHT, "ounce", 28.3495),

    K(UnitType.TEMPERATURE, "kelvin"),
    C(UnitType.TEMPERATURE, "degree Celsius", "degrees Celsius", "(?:degrees? )?Celsius".toPattern(Pattern.CASE_INSENSITIVE), { it + 273.15 }, { it - 273.15 }),
    DC(UnitType.TEMPERATURE, "degree Celsius", "degrees Celsius", "(?:degrees? )?Celsius".toPattern(Pattern.CASE_INSENSITIVE), { it + 273.15 }, { it - 273.15 }),
    F(UnitType.TEMPERATURE, "degree Fahrenheit", "degrees Fahrenheit", "(?:degrees? )?Fahrenheit".toPattern(Pattern.CASE_INSENSITIVE), { 5 * (it + 459.67) / 9 }, { 9 * it / 5 - 459.67 }),
    DF(UnitType.TEMPERATURE, "degree Fahrenheit", "degrees Fahrenheit", "(?:degrees? )?Fahrenheit".toPattern(Pattern.CASE_INSENSITIVE), { 5 * (it + 459.67) / 9 }, { 9 * it / 5 - 459.67 }),
    ;

    constructor(type: UnitType, singular: String, factorToBase: Double = 1.0, plural: String = "${singular}s") :
            this(type, singular, plural, "(?:$singular|$plural)".toPattern(Pattern.CASE_INSENSITIVE), { it * factorToBase } , {it / factorToBase })

    companion object {
        fun parse(string: String): Units? {
            val uppercase = string.uppercase()
            for (unit in values()) {
                if (uppercase == unit.name || unit.pattern.matcher(uppercase).matches())
                    return unit
            }
            return null
        }

        fun print(unitsOrNull : Units?): String {
            return unitsOrNull?.plural ?: "???"
        }
    }

    fun toString(value: Double): String {
        return if (value == 1.0)
            "1.0 ${this.singular}"
        else
            "$value ${this.plural}"
    }
}

val PATTERN_QUERY = "(?<numberIn>-?\\d+(?:\\.\\d+)?) (?<measureIn>(?:degrees? )?\\w+) (?:\\w+) (?<measureOut>(?:degrees? )?\\w+)".toPattern(Pattern.CASE_INSENSITIVE)

fun main() {
    while (true) {
        println("Enter what you want to convert (or exit): ")
        val line = readLine()!!

        if (line == "exit")
            break

        val matcher = PATTERN_QUERY.matcher(line)
        if (matcher.matches()) {
            val numberIn = matcher.group("numberIn").toDouble()
            val measureIn = matcher.group("measureIn")
            val unitsIn = Units.parse(measureIn)

            val measureOut = matcher.group("measureOut")
            val unitsOut = Units.parse(measureOut)

            if (unitsIn != null && unitsOut != null) {
                if (!unitsIn.type.allowsNegatives && numberIn < 0) {
                    println("${unitsIn.type.name.first().uppercase()}${unitsIn.type.name.substring(1).lowercase()} shouldn't be negative")
                } else if (unitsIn.type == unitsOut.type) {
                    val inBaseUnits = unitsIn.toBase(numberIn)
                    val inOutUnits = unitsOut.fromBase(inBaseUnits)

                    println("${unitsIn.toString(numberIn)} is ${unitsOut.toString(inOutUnits)}")
                } else
                    println("Conversion from ${unitsIn.plural.lowercase()} to ${unitsOut.plural.lowercase()} is impossible")
            } else {
                println("Conversion from ${Units.print(unitsIn)} to ${Units.print(unitsOut)} is impossible")
            }
        } else {
            println("Parse error")
        }
        println()
    }
}
