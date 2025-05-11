package com.mrboomdev.awery.utils.annotations

@RequiresOptIn(
	message = "Warning! Direct usage of this api is a bad practice and may lead to crashes! " +
			"Opt in only if you REALLY do know are you doing!",
	level = RequiresOptIn.Level.ERROR)
@Target(
	AnnotationTarget.CLASS,
	AnnotationTarget.PROPERTY,
	AnnotationTarget.FIELD,
	AnnotationTarget.VALUE_PARAMETER,
	AnnotationTarget.CONSTRUCTOR,
	AnnotationTarget.FUNCTION,
	AnnotationTarget.PROPERTY_SETTER)
annotation class AweryInternalApi