package com.gcaguilar.biciradar.appfunctions

import androidx.appfunctions.AppFunctionAppUnknownException
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionInvalidArgumentException

/**
 * Creates the AppFunctions request errors thrown by [BiciRadarAppFunctions].
 *
 * This factory keeps the orchestration testable on the JVM: the real
 * AppFunctions exceptions read `android.os.Bundle.EMPTY`, which is null in the
 * unit-test Android stub, so tests inject a fake that produces plain JVM
 * exceptions. Production always uses [DefaultAppFunctionErrors], which creates
 * the exception types the framework expects.
 */
internal interface AppFunctionErrors {
  fun invalidArgument(message: String): Exception

  fun elementNotFound(message: String): Exception

  fun appUnknown(message: String): Exception
}

/** Production [AppFunctionErrors] backed by the AppFunctions exception types. */
internal object DefaultAppFunctionErrors : AppFunctionErrors {
  override fun invalidArgument(message: String): Exception = AppFunctionInvalidArgumentException(message)

  override fun elementNotFound(message: String): Exception = AppFunctionElementNotFoundException(message)

  override fun appUnknown(message: String): Exception = AppFunctionAppUnknownException(message)
}
