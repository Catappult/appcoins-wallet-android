@file:OptIn(ExperimentalContracts::class, ExperimentalCoroutinesApi::class)
@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED", "TestFunctionName")

package com.asfoundation.wallet

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


object Gherkin {
  val calls = mutableListOf<Int>()
  val names = mutableSetOf<String>()
}

object GherkinClass

fun gherkin(test: Gherkin.() -> Unit) {
  contract {
    callsInPlace(test, InvocationKind.EXACTLY_ONCE)
  }
  Gherkin.calls.clear()
  Gherkin.names.clear()
  Gherkin.test()
  require(Gherkin.calls.contains(0)) { "'Given' is missing" }
  require(Gherkin.calls.contains(1)) { "'When' is missing" }
  require(Gherkin.calls.contains(2)) { "'Then' is missing" }
}

fun coGherkin(test: suspend Gherkin.(scope: TestScope) -> Unit) {
  contract {
    callsInPlace(test, InvocationKind.EXACTLY_ONCE)
  }
  Gherkin.calls.clear()
  Gherkin.names.clear()
  runTest {
    Gherkin.test(this)
  }
  require(Gherkin.calls.contains(0)) { "'Given' is missing" }
  require(Gherkin.calls.contains(1)) { "'When' is missing" }
  require(Gherkin.calls.contains(2)) { "'Then' is missing" }
}

inline fun Gherkin.Given(desc: String, block: GherkinClass.() -> Unit): GherkinClass {
  contract {
    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
  }
  require(desc.isNotBlank()) { "'Given' description should not be blank" }
  require(calls.contains(0).not()) { "Only one 'Given' is allowed" }
  require(calls.isEmpty()) { "'Given' should be the first one to call" }
  require(names.contains(desc).not()) { "Duplicated description: $desc" }
  GherkinClass.block()
  calls.add(0)
  names.add(desc)
  return GherkinClass
}

inline fun Gherkin.When(desc: String, block: GherkinClass.() -> Unit): GherkinClass {
  contract {
    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
  }
  require(desc.isNotBlank()) { "'When' description should not be blank" }
  require(calls.contains(0)) { "'When' should be called after 'Given'" }
  require(calls.contains(1).not()) { "Only one 'When' is allowed" }
  require(names.contains(desc).not()) { "Duplicated description: $desc" }
  GherkinClass.block()
  calls.add(1)
  names.add(desc)
  return GherkinClass
}

inline fun Gherkin.Then(desc: String, block: GherkinClass.() -> Unit): GherkinClass {
  contract {
    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
  }
  require(desc.isNotBlank()) { "'Then' description should not be blank" }
  require(calls.contains(1)) { "'Then' should be called after 'When'" }
  require(calls.contains(2).not()) { "Only one 'Then' is allowed" }
  require(names.contains(desc).not()) { "Duplicated description: $desc" }
  GherkinClass.block()
  calls.add(2)
  names.add(desc)
  return GherkinClass
}

inline fun GherkinClass.and(desc: String, block: GherkinClass.() -> Unit): GherkinClass {
  contract {
    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
  }
  require(Gherkin.names.contains(desc).not()) { "Duplicated description: $desc" }
  block()
  Gherkin.names.add(desc)
  return this
}

inline fun GherkinClass.but(desc: String, block: GherkinClass.() -> Unit): GherkinClass {
  contract {
    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
  }
  require(Gherkin.names.contains(desc).not()) { "Duplicated description: $desc" }
  block()
  Gherkin.names.add(desc)
  return this
}

class GherkinTests {

  @Nested
  inner class GherkinTest {

    @Test
    fun `Minimal test`() = gherkin {
      Given("Given persists") {}
      When("When persists") {}
      Then("Then persists") {}
    }

    @Test
    fun `Maximal test`() = gherkin {
      Given("Given persists") {}
        .and("Given And persists") {}
        .but("Given But persists") {}
      When("When persists") {}
        .and("When And persists") {}
        .but("When But persists") {}
      Then("Then persists") {}
        .and("Then And persists") {}
        .but("Then But persists") {}
    }

    @Test
    fun `Double Given test`() {
      val thrown: IllegalArgumentException = assertThrows {
        gherkin {
          Given("Given persists") {}
          Given("Given persists") {}
          When("When persists") {}
          Then("Then persists") {}
        }
      }
      assertEquals("Only one 'Given' is allowed", thrown.message)
    }

    @Test
    fun `Double When test`() {
      val thrown: IllegalArgumentException = assertThrows {
        gherkin {
          Given("Given persists") {}
          When("When persists") {}
          When("When persists") {}
          Then("Then persists") {}
        }
      }
      assertEquals("Only one 'When' is allowed", thrown.message)
    }

    @Test
    fun `Double Then test`() {
      val thrown: IllegalArgumentException = assertThrows {
        gherkin {
          Given("Given persists") {}
          When("When persists") {}
          Then("Then persists") {}
          Then("Then persists") {}
        }
      }
      assertEquals("Only one 'Then' is allowed", thrown.message)
    }

    @Test
    fun `Missing Given in test`() {
      val thrown: IllegalArgumentException = assertThrows {
        gherkin {
          When("When persists") {}
          Then("Then persists") {}
        }
      }
      assertEquals("'When' should be called after 'Given'", thrown.message)
    }

    @Test
    fun `Missing When in test`() {
      val thrown: IllegalArgumentException = assertThrows {
        gherkin {
          Given("Given persists") {}
          Then("Then persists") {}
        }
      }
      assertEquals("'Then' should be called after 'When'", thrown.message)
    }

    @Test
    fun `Missing Then in test`() {
      val thrown: IllegalArgumentException = assertThrows {
        gherkin {
          Given("Given persists") {}
          When("When persists") {}
        }
      }
      assertEquals("'Then' is missing", thrown.message)
    }

    @Test
    fun `Only Given in test`() {
      val thrown: IllegalArgumentException = assertThrows {
        gherkin {
          Given("Given persists") {}
        }
      }
      assertEquals("'When' is missing", thrown.message)
    }

    @Test
    fun `Only When in test`() {
      val thrown: IllegalArgumentException = assertThrows {
        gherkin {
          When("When persists") {}
        }
      }
      assertEquals("'When' should be called after 'Given'", thrown.message)
    }

    @Test
    fun `Only Then in test`() {
      val thrown: IllegalArgumentException = assertThrows {
        gherkin {
          Then("Then persists") {}
        }
      }
      assertEquals("'Then' should be called after 'When'", thrown.message)
    }

    @Test
    fun `Folded When test`() {
      val thrown: IllegalArgumentException = assertThrows {
        gherkin {
          Given("Given persists") {
            When("When persists") {}
          }
          Then("Then persists") {}
        }
      }
      assertEquals("'When' should be called after 'Given'", thrown.message)
    }

    @Test
    fun `Folded Then test`() {
      val thrown: IllegalArgumentException = assertThrows {
        gherkin {
          Given("Given persists") {}
          When("When persists") {
            Then("Then persists") {}
          }
        }
      }
      assertEquals("'Then' should be called after 'When'", thrown.message)
    }

    @Test
    fun `Folded When Then test`() {
      val thrown: IllegalArgumentException = assertThrows {
        gherkin {
          Given("Given persists") {
            When("When persists") {}
            Then("Then persists") {}
          }
        }
      }
      assertEquals("'When' should be called after 'Given'", thrown.message)
    }

    @Test
    fun `Folded All keywords test`() {
      val thrown: IllegalArgumentException = assertThrows {
        gherkin {
          Given("Given persists") {
            When("When persists") {
              Then("Then persists") {}
            }
          }
        }
      }
      assertEquals("'When' should be called after 'Given'", thrown.message)
    }

    @Test
    fun `Duplicated docs test`() {
      val thrown: IllegalArgumentException = assertThrows {
        gherkin {
          Given("Given persists") {}
          When("When persists") {}
          Then("Given persists") {}
        }
      }
      assertEquals("Duplicated description: Given persists", thrown.message)
    }
  }

  @Nested
  inner class CoGherkinTest {

    @Test
    fun `Minimal test`() = coGherkin {
      Given("Given persists") {}
      When("When persists") {}
      Then("Then persists") {}
    }

    @Test
    fun `Maximal test`() = coGherkin {
      Given("Given persists") {}
        .and("Given And persists") {}
        .but("Given But persists") {}
      When("When persists") {}
        .and("When And persists") {}
        .but("When But persists") {}
      Then("Then persists") {}
        .and("Then And persists") {}
        .but("Then But persists") {}
    }


    @Test
    fun `Double Given test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coGherkin {
          Given("Given persists") {}
          Given("Given persists") {}
          When("When persists") {}
          Then("Then persists") {}
        }
      }
      assertEquals("Only one 'Given' is allowed", thrown.message)
    }

    @Test
    fun `Double When test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coGherkin {
          Given("Given persists") {}
          When("When persists") {}
          When("When persists") {}
          Then("Then persists") {}
        }
      }
      assertEquals("Only one 'When' is allowed", thrown.message)
    }

    @Test
    fun `Double Then test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coGherkin {
          Given("Given persists") {}
          When("When persists") {}
          Then("Then persists") {}
          Then("Then persists") {}
        }
      }
      assertEquals("Only one 'Then' is allowed", thrown.message)
    }

    @Test
    fun `Missing Given in test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coGherkin {
          When("When persists") {}
          Then("Then persists") {}
        }
      }
      assertEquals("'When' should be called after 'Given'", thrown.message)
    }

    @Test
    fun `Missing When in test`() {
      val thrown: IllegalArgumentException = assertThrows {
        runTest {
          gherkin {
            Given("Given persists") {}
            Then("Then persists") {}
          }
        }
      }
      assertEquals("'Then' should be called after 'When'", thrown.message)
    }

    @Test
    fun `Missing Then in test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coGherkin {
          Given("Given persists") {}
          When("When persists") {}
        }
      }
      assertEquals("'Then' is missing", thrown.message)
    }

    @Test
    fun `Only Given in test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coGherkin {
          Given("Given persists") {}
        }
      }
      assertEquals("'When' is missing", thrown.message)
    }

    @Test
    fun `Only When in test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coGherkin {
          When("When persists") {}
        }
      }
      assertEquals("'When' should be called after 'Given'", thrown.message)
    }

    @Test
    fun `Only Then in test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coGherkin {
          Then("Then persists") {}
        }
      }
      assertEquals("'Then' should be called after 'When'", thrown.message)
    }

    @Test
    fun `Folded When test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coGherkin {
          Given("Given persists") {
            When("When persists") {}
          }
          Then("Then persists") {}
        }
      }
      assertEquals("'When' should be called after 'Given'", thrown.message)
    }

    @Test
    fun `Folded Then test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coGherkin {
          Given("Given persists") {}
          When("When persists") {
            Then("Then persists") {}
          }
        }
      }
      assertEquals("'Then' should be called after 'When'", thrown.message)
    }

    @Test
    fun `Folded When Then test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coGherkin {
          Given("Given persists") {
            When("When persists") {}
            Then("Then persists") {}
          }
        }
      }
      assertEquals("'When' should be called after 'Given'", thrown.message)
    }

    @Test
    fun `Folded All keywords test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coGherkin {
          Given("Given persists") {
            When("When persists") {
              Then("Then persists") {}
            }
          }
        }
      }
      assertEquals("'When' should be called after 'Given'", thrown.message)
    }

    @Test
    fun `Duplicated docs test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coGherkin {
          Given("Given persists") {}
          When("When persists") {}
          Then("Given persists") {}
        }
      }
      assertEquals("Duplicated description: Given persists", thrown.message)
    }
  }
}
