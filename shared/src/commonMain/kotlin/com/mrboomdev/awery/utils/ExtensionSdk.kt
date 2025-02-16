package com.mrboomdev.awery.utils

/**
 * Annotated thing is a member of an public extension sdk,
 * so hence it cannot be removed for compatibility reasons.
 * 
 * This code may be unused by the host application,
 * but we don't know if extensions do use it or no, so we keep it.
 */
@Retention(AnnotationRetention.SOURCE)
annotation class ExtensionSdk(
	val since: String = ""
)