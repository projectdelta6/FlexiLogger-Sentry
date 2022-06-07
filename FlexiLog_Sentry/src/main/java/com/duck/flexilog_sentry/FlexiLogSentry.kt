package com.duck.flexilog_sentry

import com.duck.flexilogger.FlexiLog
import com.duck.flexilogger.LogType
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.protocol.Message
import org.jetbrains.annotations.Contract

abstract class FlexiLogSentry: FlexiLog() {
	/**
	 * Implement the actual reporting.
	 *
	 * @param type [Int] @[LogType], the type of log this came from.
	 * @param tag [Class] The Log tag
	 * @param msg [String] The Log message.
	 */
	override fun report(type: LogType, tag: String, msg: String) {
		Sentry.captureEvent(SentryEvent().apply {
			message = Message().apply { message = msg }
			level = getLevel(type)
			logger = tag
		})
	}

	/**
	 * Implement the actual reporting.
	 *
	 * @param type [Int] @[LogType], the type of log this came from.
	 * @param tag [Class] The Log tag
	 * @param msg [String] The Log message.
	 * @param tr  [Throwable] to be attached to the Log.
	 */
	override fun report(type: LogType, tag: String, msg: String, tr: Throwable) {
		Sentry.captureEvent(SentryEvent().apply {
			message = Message().apply { message = msg }
			level = getLevel(type)
			logger = tag
			throwable = tr
		})
	}

	@Contract(pure = true)
	private fun getLevel(type: LogType): SentryLevel {
		return when (type) {
			LogType.E -> SentryLevel.ERROR
			LogType.D, LogType.V, LogType.WTF -> SentryLevel.DEBUG
			LogType.I -> SentryLevel.INFO
			LogType.W -> SentryLevel.WARNING
		}
	}

	//------ Breadcrumbs -------

	/**
	 * Create a breadcrumb
	 *
	 * @param caller Any object, the Class name will be used as the 'tag' in the breadcrumb message
	 * @param message The message for the breadcrumb
	 */
	fun breadCrumb(caller: Any, message: String, category: String = "") {
		breadCrumb(getClassName(caller), message, category)
	}

	/**
	 * Create a breadcrumb
	 *
	 * @param caller Class object, the Class name will be used as the 'tag' in the breadcrumb message
	 * @param message The message for the breadcrumb
	 */
	fun breadCrumb(caller: Class<*>, message: String, category: String = "") {
		breadCrumb(getClassName(caller), message, category)
	}

	/**
	 * Create a breadcrumb
	 *
	 * @param tag String used to construct the breadcrumb message using [formatBreadcrumbMessage]
	 * @param message The message for the breadcrumb
	 */
	fun breadCrumb(tag: String, message: String, category: String = "") {
		if(shouldLogBreadcrumbs()) {
			breadCrumb(formatBreadcrumbMessage(tag, message), category)
		}
	}

	/**
	 * Create a breadcrumb
	 *
	 * @param message The message for the breadcrumb
	 */
	fun breadCrumb(message: String, category: String = "") {
		if(shouldLogBreadcrumbs()) {
			Sentry.addBreadcrumb(message, category)
		}
	}

	/**
	 * Create a breadcrumb from a Sentry [Breadcrumb] object.
	 */
	fun breadcrumb(breadcrumb: Breadcrumb) {
		if(shouldLogBreadcrumbs()) {
			Sentry.addBreadcrumb(breadcrumb)
		}
	}

	/**
	 * Join the [tag] and [message] into a single string.
	 *
	 * The base implementation constructs the string with the format "[tag]::[message]"
	 */
	open fun formatBreadcrumbMessage(tag: String, message: String): String {
		return if(tag.isBlank()) message else "$tag::$message"
	}

	/**
	 * Determine if the breadcrumb should be reported or not.
	 */
	protected abstract fun shouldLogBreadcrumbs(): Boolean

	/**
	 * Clear captured breadCrumbs.
	 */
	fun clearBreadCrumbs() {
		Sentry.clearBreadcrumbs()
	}
}