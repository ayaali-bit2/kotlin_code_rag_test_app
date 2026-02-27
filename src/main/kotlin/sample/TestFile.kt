package sample

interface SampleInterface {
    fun execute(): String
}

enum class SampleStatus {
    PENDING,
    COMPLETED
}

data class SampleData(
    val id: Int,
    val label: String
)

class SampleClass : SampleInterface {
    override fun execute(): String = "Executing sample service"
}

fun sampleTopLevelFunction() {}
