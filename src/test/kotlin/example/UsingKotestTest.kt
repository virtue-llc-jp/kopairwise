package example

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import llc.virtue.kopairwise.Factor
import llc.virtue.kopairwise.eq
import llc.virtue.kopairwise.generatePairwise
import llc.virtue.kopairwise.neq

class UsingKotestTest: FunSpec({
    context("Ticket variation") {
        val factors = listOf(
            Factor(SeatClass::class),
            Factor(PassengerType::class),
            Factor(MealOption::class),
        )
        val constraints = listOf(
            "SeatClass" eq SeatClass.FIRST implies ("PassengerType" neq PassengerType.CHILD),
            "SeatClass" eq SeatClass.ECONOMY implies ("MealOption" neq MealOption.SPECIAL),
        )
        val testCases = generatePairwise(factors, constraints)
        testCases.forEach { testCase ->
            val (seatClass, passengerType, mealOption) = Triple(
                testCase["SeatClass"] as SeatClass,
                testCase["PassengerType"] as PassengerType,
                testCase["MealOption"] as MealOption,
            )
            test("SeatClass=${seatClass}, PassengerType=${passengerType}, MealOption-=${mealOption}") {
                val result = calculateFlightTicketPrice(seatClass, passengerType, mealOption)
                result shouldBeGreaterThan 0.0
            }
        }
        val invalidTestCases = generatePairwise(factors, constraints, true)
        invalidTestCases.forEach { testCase ->
            val (seatClass, passengerType, mealOption) = Triple(
                testCase["SeatClass"] as SeatClass,
                testCase["PassengerType"] as PassengerType,
                testCase["MealOption"] as MealOption,
            )
            test("invalid combination: SeatClass=${seatClass}, PassengerType=${passengerType}, MealOption-=${mealOption}") {
                shouldThrow<InvalidFlightOptionException> {
                    calculateFlightTicketPrice(seatClass, passengerType, mealOption)
                }
            }
        }
    }
})
