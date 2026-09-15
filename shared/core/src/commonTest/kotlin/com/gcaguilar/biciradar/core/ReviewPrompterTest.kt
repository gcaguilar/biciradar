package com.gcaguilar.biciradar.core

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReviewPrompterTest {
  private class FakeReviewPrompter(
    var inAppReviewResult: Boolean = true,
  ) : ReviewPrompter {
    var inAppReviewCalls = 0
    var storeReviewCalls = 0

    override suspend fun requestInAppReview(): Boolean {
      inAppReviewCalls++
      return inAppReviewResult
    }

    override fun openStoreWriteReview() {
      storeReviewCalls++
    }
  }

  @Test
  fun `fallback opens the store when in-app review cannot be attempted`() {
    runTest {
      val prompter = FakeReviewPrompter(inAppReviewResult = false)

      prompter.requestInAppReviewOrStoreFallback()

      assertEquals(1, prompter.inAppReviewCalls)
      assertEquals(1, prompter.storeReviewCalls)
    }
  }

  @Test
  fun `fallback skips the store when in-app review was attempted`() {
    runTest {
      val prompter = FakeReviewPrompter(inAppReviewResult = true)

      prompter.requestInAppReviewOrStoreFallback()

      assertEquals(1, prompter.inAppReviewCalls)
      assertEquals(0, prompter.storeReviewCalls)
    }
  }

  @Test
  fun `requestInAppReview reports whether the platform call happened`() {
    runTest {
      val unavailable = FakeReviewPrompter(inAppReviewResult = false)
      val attempted = FakeReviewPrompter(inAppReviewResult = true)

      assertFalse(unavailable.requestInAppReview())
      assertTrue(attempted.requestInAppReview())
    }
  }
}
