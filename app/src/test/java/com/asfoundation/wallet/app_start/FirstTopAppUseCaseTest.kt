package com.asfoundation.wallet.app_start

import com.asfoundation.wallet.app_start.FirstTopAppUseCaseImpl.Companion.TOP_APPS_PACKAGES
import com.asfoundation.wallet.gherkin.coScenario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.streams.toList

/**
 * AS a Wallet Developer,
 * I WANT to select the one and only top app if installed,
 * FOR separation concerns
 */

@ExperimentalCoroutinesApi
internal class FirstTopAppUseCaseTest {

  @Test
  fun `On system without top apps returns null`() = coScenario { _ ->
    val repository: FirstTopAppRepository
    val useCase: FirstTopAppUseCase

    m Given "Repository returns list without top apps"
    repository = RepositoryMock()
    useCase = FirstTopAppUseCaseImpl(repository)

    m When "Use case called for result"
    val result = useCase()

    m Then "result is null"
    assertNull(result)
  }

  @Test
  fun `On system with 1 top app returns FirstTopApp`() = coScenario { _ ->
    val repository: FirstTopAppRepository
    val useCase: FirstTopAppUseCase

    m Given "Repository returns list with 1 top app"
    repository = RepositoryMock(installedApps = listOf(TOP_APPS_PACKAGES[2]))
    useCase = FirstTopAppUseCaseImpl(repository)

    m When "Use case called for result"
    val result = useCase()

    m Then "result is data"
    assertEquals(
      StartMode.FirstTopApp(packageName = TOP_APPS_PACKAGES[2]),
      result
    )
  }

  @Test
  fun `On system with 2 top apps returns null`() = coScenario { _ ->
    val repository: FirstTopAppRepository
    val useCase: FirstTopAppUseCase

    m Given "Repository returns random list with 2 top apps"
    repository = RepositoryMock(
      installedApps = listOf(TOP_APPS_PACKAGES[0], TOP_APPS_PACKAGES[2])
    )
    useCase = FirstTopAppUseCaseImpl(repository)

    m When "Use case called for result"
    val result = useCase()

    m Then "result is null"
    assertNull(result)
  }

  @Test
  fun `On system with 3 top apps returns null`() = coScenario { _ ->
    val repository: FirstTopAppRepository
    val useCase: FirstTopAppUseCase

    m Given "Repository returns random list with 3 top apps"
    repository = RepositoryMock(
      installedApps = listOf(TOP_APPS_PACKAGES[0], TOP_APPS_PACKAGES[2], TOP_APPS_PACKAGES[4])
    )
    useCase = FirstTopAppUseCaseImpl(repository)

    m When "Use case called for result"
    val result = useCase()

    m Then "result is null"
    assertNull(result)
  }

  @Test
  fun `On system with 4 top apps returns null`() = coScenario { _ ->
    val repository: FirstTopAppRepository
    val useCase: FirstTopAppUseCase

    m Given "Repository returns random list with 4 top apps"
    repository = RepositoryMock(
      installedApps = listOf(
        TOP_APPS_PACKAGES[0],
        TOP_APPS_PACKAGES[2],
        TOP_APPS_PACKAGES[4],
        TOP_APPS_PACKAGES[1]
      )
    )
    useCase = FirstTopAppUseCaseImpl(repository)

    m When "Use case called for result"
    val result = useCase()

    m Then "result is null"
    assertNull(result)
  }

  @Test
  fun `On system with all top apps returns null`() = coScenario { _ ->
    val repository: FirstTopAppRepository
    val useCase: FirstTopAppUseCase

    m Given "Repository returns random list with all top apps"
    repository = RepositoryMock(installedApps = TOP_APPS_PACKAGES)
    useCase = FirstTopAppUseCaseImpl(repository)

    m When "Use case called for result"
    val result = useCase()

    m Then "result is null"
    assertNull(result)
  }

  private class RepositoryMock(
    private val installedApps: List<String> = listOf(),
  ) : FirstTopAppRepository {
    // Random 20 - 99 existing installed packages
    private val packages = "abcdefghijklmnopqrstuvwxyz.".run {
      (1..Random.nextInt(20, 100))
        .map {
          java.util.Random().ints(Random.nextLong(11, 33), 0, length)
            .toList()
            .map(::get)
            .joinToString("")
        }
    }

    override suspend fun getInstalledPackages(): List<String> {
      delay(100)
      // Randomly mix with the existing installed packages
      return (installedApps + packages).shuffled()
    }
  }
}