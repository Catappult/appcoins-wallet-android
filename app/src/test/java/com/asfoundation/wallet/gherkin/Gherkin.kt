@file:OptIn(ExperimentalCoroutinesApi::class)

package com.asfoundation.wallet.gherkin

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

object Gherkin {
  internal var m: Step = Step()
    set(value) {
      require(busy.not()) { "Gherkin is busy" }
      field = value
    }
  internal var busy = false
}

fun scenario(test: Gherkin.() -> Unit) {
  require(Gherkin.busy.not()) { "scenarios folding is forbidden" }
  try {
    Gherkin.m = Step()
    Gherkin.busy = true
    Gherkin.test()
    Gherkin.m.test()
  } finally {
    Gherkin.busy = false
  }
}

@ExperimentalCoroutinesApi
fun coScenario(test: suspend Gherkin.(scope: TestScope) -> Unit) {
  require(Gherkin.busy.not()) { "coScenarios folding is forbidden" }
  try {
    Gherkin.m = Step()
    Gherkin.busy = true
    runTest {
      Gherkin.test(this)
    }
    Gherkin.m.test()
  } finally {
    Gherkin.busy = false
  }
}

@Suppress("TestFunctionName")
class Step {
  private val calls: MutableSet<Int> = mutableSetOf()
  private val names: MutableSet<String> = mutableSetOf()

  infix fun Given(description: String) {
    require(description.isNotBlank()) { "'Given' description should not be blank" }
    require(calls.contains(0).not()) { "Only one 'Given' is allowed" }
    require(calls.isEmpty()) { "'Given' should be the first one to call" }
    require(names.contains(description).not()) { "Duplicated description: $description" }
    calls.add(0)
    names.add(description)
  }

  infix fun When(description: String) {
    require(description.isNotBlank()) { "'When' description should not be blank" }
    require(calls.contains(0)) { "'When' should be called after 'Given'" }
    require(calls.contains(1).not()) { "Only one 'When' is allowed" }
    require(names.contains(description).not()) { "Duplicated description: $description" }
    calls.add(1)
    names.add(description)
  }

  infix fun Then(description: String) {
    require(description.isNotBlank()) { "'Then' description should not be blank" }
    require(calls.contains(1)) { "'Then' should be called after 'When'" }
    require(calls.contains(2).not()) { "Only one 'Then' is allowed" }
    require(names.contains(description).not()) { "Duplicated description: $description" }
    calls.add(2)
    names.add(description)
  }

  infix fun And(description: String) {
    require(names.contains(description).not()) { "Duplicated description: $description" }
    require(calls.isNotEmpty()) { "'And' should be called after 'Given', 'When' or 'Then'" }
    names.add(description)
  }

  infix fun But(description: String) {
    require(names.contains(description).not()) { "Duplicated description: $description" }
    require(calls.isNotEmpty()) { "'But' should be called after 'Given', 'When' or 'Then'" }
    names.add(description)
  }

  internal fun test() {
    require(calls.contains(0)) { "'Given' is missing" }
    require(calls.contains(1)) { "'When' is missing" }
    require(calls.contains(2)) { "'Then' is missing" }
  }
}

class GherkinTests {

  @Nested
  inner class ScenarioTest {

    @Test
    fun `Minimal test`() = scenario {
      m Given "Given persists"
      m When "When persists"
      m Then "Then persists"
    }

    @Test
    fun `Maximal test`() = scenario {
      m Given "Given persists"
      m And "Given And persists"
      m But "Given But persists"
      m When "When persists"
      m And "When And persists"
      m But "When But persists"
      m Then "Then persists"
      m And "Then And persists"
      m But "Then But persists"
    }

    @Test
    fun `Double Given in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        scenario {
          m Given "Given persists"
          m Given "Given persists"
          m When "When persists"
          m Then "Then persists"
        }
      }
      assertEquals("Only one 'Given' is allowed", thrown.message)
    }

    @Test
    fun `Double When in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        scenario {
          m Given "Given persists"
          m When "When persists"
          m When "When persists"
          m Then "Then persists"
        }
      }
      assertEquals("Only one 'When' is allowed", thrown.message)
    }

    @Test
    fun `Double Then in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        scenario {
          m Given "Given persists"
          m When "When persists"
          m Then "Then persists"
          m Then "Then persists"
        }
      }
      assertEquals("Only one 'Then' is allowed", thrown.message)
    }

    @Test
    fun `Missing Given in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        scenario {
          m When "When persists"
          m Then "Then persists"
        }
      }
      assertEquals("'When' should be called after 'Given'", thrown.message)
    }

    @Test
    fun `Missing When in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        scenario {
          m Given "Given persists"
          m Then "Then persists"
        }
      }
      assertEquals("'Then' should be called after 'When'", thrown.message)
    }

    @Test
    fun `Missing Then in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        scenario {
          m Given "Given persists"
          m When "When persists"
        }
      }
      assertEquals("'Then' is missing", thrown.message)
    }

    @Test
    fun `Only Given in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        scenario {
          m Given "Given persists"
        }
      }
      assertEquals("'When' is missing", thrown.message)
    }

    @Test
    fun `Only When in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        scenario {
          m When "When persists"
        }
      }
      assertEquals("'When' should be called after 'Given'", thrown.message)
    }

    @Test
    fun `Only Then in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        scenario {
          m Then "Then persists"
        }
      }
      assertEquals("'Then' should be called after 'When'", thrown.message)
    }

    @Test
    fun `First And in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        scenario {
          m And "And persists"
          m Given "Given persists"
          m When "When persists"
          m Then "Then persists"
        }
      }
      assertEquals("'And' should be called after 'Given', 'When' or 'Then'", thrown.message)
    }

    @Test
    fun `First But in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        scenario {
          m But "And persists"
          m Given "Given persists"
          m When "When persists"
          m Then "Then persists"
        }
      }
      assertEquals("'But' should be called after 'Given', 'When' or 'Then'", thrown.message)
    }

    @Test
    fun `Duplicated docs in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        scenario {
          m Given "Given persists"
          m When "When persists"
          m Then "Given persists"
        }
      }
      assertEquals("Duplicated description: Given persists", thrown.message)
    }

    @Test
    fun `Folded scenarios in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        scenario {
          m Given "Given persists"
          m When "When persists"
          m Then "Then persists"
          scenario {
            m Given "Given persists"
            m When "When persists"
            m Then "Then persists"
          }
        }
      }
      assertEquals("scenarios folding is forbidden", thrown.message)
    }

    @Test
    fun `Step overriding in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        scenario {
          m = Step()
          m Given "Given persists"
          m When "When persists"
          m Then "Then persists"
        }
      }
      assertEquals("Gherkin is busy", thrown.message)
    }
  }

  @Nested
  inner class CoScenarioTest {

    @Test
    fun `Minimal test`() = coScenario {
      m Given "Given persists"
      m When "When persists"
      m Then "Then persists"
    }

    @Test
    fun `Maximal test`() = coScenario {
      m Given "Given persists"
      m And "Given And persists"
      m But "Given But persists"
      m When "When persists"
      m And "When And persists"
      m But "When But persists"
      m Then "Then persists"
      m And "Then And persists"
      m But "Then But persists"
    }

    @Test
    fun `Double Given in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coScenario {
          m Given "Given persists"
          m Given "Given persists"
          m When "When persists"
          m Then "Then persists"
        }
      }
      assertEquals("Only one 'Given' is allowed", thrown.message)
    }

    @Test
    fun `Double When in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coScenario {
          m Given "Given persists"
          m When "When persists"
          m When "When persists"
          m Then "Then persists"
        }
      }
      assertEquals("Only one 'When' is allowed", thrown.message)
    }

    @Test
    fun `Double Then in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coScenario {
          m Given "Given persists"
          m When "When persists"
          m Then "Then persists"
          m Then "Then persists"
        }
      }
      assertEquals("Only one 'Then' is allowed", thrown.message)
    }

    @Test
    fun `Missing Given in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coScenario {
          m When "When persists"
          m Then "Then persists"
        }
      }
      assertEquals("'When' should be called after 'Given'", thrown.message)
    }

    @Test
    fun `Missing When in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coScenario {
          m Given "Given persists"
          m Then "Then persists"
        }
      }
      assertEquals("'Then' should be called after 'When'", thrown.message)
    }

    @Test
    fun `Missing Then in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coScenario {
          m Given "Given persists"
          m When "When persists"
        }
      }
      assertEquals("'Then' is missing", thrown.message)
    }

    @Test
    fun `Only Given in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coScenario {
          m Given "Given persists"
        }
      }
      assertEquals("'When' is missing", thrown.message)
    }

    @Test
    fun `Only When in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coScenario {
          m When "When persists"
        }
      }
      assertEquals("'When' should be called after 'Given'", thrown.message)
    }

    @Test
    fun `Only Then in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coScenario {
          m Then "Then persists"
        }
      }
      assertEquals("'Then' should be called after 'When'", thrown.message)
    }

    @Test
    fun `First And in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coScenario {
          m And "And persists"
          m Given "Given persists"
          m When "When persists"
          m Then "Then persists"
        }
      }
      assertEquals("'And' should be called after 'Given', 'When' or 'Then'", thrown.message)
    }

    @Test
    fun `First But in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coScenario {
          m But "And persists"
          m Given "Given persists"
          m When "When persists"
          m Then "Then persists"
        }
      }
      assertEquals("'But' should be called after 'Given', 'When' or 'Then'", thrown.message)
    }

    @Test
    fun `Duplicated docs in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coScenario {
          m Given "Given persists"
          m When "When persists"
          m Then "Given persists"
        }
      }
      assertEquals("Duplicated description: Given persists", thrown.message)
    }

    @Test
    fun `Folded coScenarios in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coScenario {
          m Given "Given persists"
          m When "When persists"
          m Then "Then persists"
          coScenario {
            m Given "Given persists"
            m When "When persists"
            m Then "Then persists"
          }
        }
      }
      assertEquals("coScenarios folding is forbidden", thrown.message)
    }

    @Test
    fun `Step overriding in a test`() {
      val thrown: IllegalArgumentException = assertThrows {
        coScenario {
          m = Step()
          m Given "Given persists"
          m When "When persists"
          m Then "Then persists"
        }
      }
      assertEquals("Gherkin is busy", thrown.message)
    }
  }
}
