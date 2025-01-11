package example

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import llc.virtue.kopairwise.*

class UsingKotestTest : FunSpec({
    context("Ticket variation") {
        class Case(
            var seatClass: SeatClass,
            var passengerType: PassengerType,
            var mealOption: MealOption,
        )

        val pairwise = Pairwise {
            factors(
                SeatClass::class,
                PassengerType::class,
                MealOption::class,
            )
            constraints(
                "seatClass" eq SeatClass.FIRST implies ("passengerType" neq PassengerType.CHILD),
                "seatClass" eq SeatClass.ECONOMY implies ("mealOption" neq MealOption.SPECIAL)
            )
        }

        val testCases = pairwise.cases(Case::class)
        testCases.forEach {
            with(it) {
                test("seatClass=${seatClass}, passengerType=${passengerType}, mealOption-=${mealOption}") {
                    val result = calculateFlightTicketPrice(seatClass, passengerType, mealOption)
                    result shouldBeGreaterThan 0.0
                }
            }
        }

        val invalidTestCases = pairwise.invertCases(Case::class)
        invalidTestCases.forEach {
            with(it) {
                test("invalid combination: seatClass=${seatClass}, passengerType=${passengerType}, mealOption-=${mealOption}") {
                    shouldThrow<InvalidFlightOptionException> {
                        calculateFlightTicketPrice(seatClass, passengerType, mealOption)
                    }
                }
            }
        }
    }
})
