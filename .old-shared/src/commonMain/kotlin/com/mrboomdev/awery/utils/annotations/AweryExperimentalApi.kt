package com.mrboomdev.awery.utils.annotations

@RequiresOptIn(
	message = "Warning! This functionality is still under work and requires an explicit opt in!",
	level = RequiresOptIn.Level.ERROR
)
@Target(
	AnnotationTarget.CLASS,
	AnnotationTarget.PROPERTY,
	AnnotationTarget.FIELD,
	AnnotationTarget.VALUE_PARAMETER,
	AnnotationTarget.CONSTRUCTOR,
	AnnotationTarget.FUNCTION,
	AnnotationTarget.PROPERTY_SETTER,
	AnnotationTarget.LOCAL_VARIABLE,
	AnnotationTarget.TYPEALIAS,
	AnnotationTarget.ANNOTATION_CLASS,
	AnnotationTarget.PROPERTY_GETTER
)
annotation class AweryExperimentalApi(val message: String = "")