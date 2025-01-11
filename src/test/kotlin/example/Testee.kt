package example

enum class SeatClass {
    ECONOMY,
    BUSINESS,
    FIRST,
}

enum class PassengerType {
    SENIOR,
    ADULT,
    CHILD,
}

enum class MealOption {
    VEGETARIAN,
    NON_VEGETARIAN,
    SPECIAL,
}

object InvalidFlightOptionException: Exception("Invalid combination") {
    private fun readResolve(): Any = InvalidFlightOptionException
}

fun calculateFlightTicketPrice(
    seatClass: SeatClass,
    passengerType: PassengerType,
    mealOption: MealOption,
): Double {
    return when (seatClass) {
        SeatClass.ECONOMY -> calculateEconomyPrice(passengerType, mealOption)
        SeatClass.BUSINESS -> calculateBusinessPrice(passengerType, mealOption)
        SeatClass.FIRST -> calculateFirstPrice(passengerType, mealOption)
    }
}

fun calculateEconomyPrice(passengerType: PassengerType, mealOption: MealOption): Double {
    return when (passengerType) {
        PassengerType.SENIOR -> 100.0
        PassengerType.ADULT -> 200.0
        PassengerType.CHILD -> 150.0
    } + when (mealOption) {
        MealOption.VEGETARIAN -> 20.0
        MealOption.NON_VEGETARIAN -> 30.0
        MealOption.SPECIAL -> throw InvalidFlightOptionException
    }
}

fun calculateBusinessPrice(passengerType: PassengerType, mealOption: MealOption): Double {
    return when (passengerType) {
        PassengerType.SENIOR -> 200.0
        PassengerType.ADULT -> 300.0
        PassengerType.CHILD -> 250.0
    } + when (mealOption) {
        MealOption.VEGETARIAN -> 40.0
        MealOption.NON_VEGETARIAN -> 60.0
        MealOption.SPECIAL -> 80.0
    }
}

fun calculateFirstPrice(passengerType: PassengerType, mealOption: MealOption): Double {
    return when (passengerType) {
        PassengerType.SENIOR -> 300.0
        PassengerType.ADULT -> 400.0
        PassengerType.CHILD -> throw InvalidFlightOptionException
    } + when (mealOption) {
        MealOption.VEGETARIAN -> 60.0
        MealOption.NON_VEGETARIAN -> 80.0
        MealOption.SPECIAL -> 100.0
    }
}
