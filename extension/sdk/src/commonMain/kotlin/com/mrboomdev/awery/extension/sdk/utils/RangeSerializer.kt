package com.mrboomdev.awery.extension.sdk.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer

object IntRangeSerializer: KSerializer<IntRange> {
	private val serializer = serializer<List<Int>>()
	
	override val descriptor: SerialDescriptor
		get() = serialDescriptor<List<Int>>()

	override fun serialize(encoder: Encoder, value: IntRange) {
		encoder.encodeSerializableValue(
			serializer,
			listOf(value.first, value.last)
		)
	}

	override fun deserialize(decoder: Decoder): IntRange {
		return decoder.decodeSerializableValue(serializer).let {
			IntRange(it[0], it[1])
		}
	}
}

object LongRangeSerializer: KSerializer<LongRange> {
	private val serializer = serializer<List<Long>>()

	override val descriptor: SerialDescriptor
		get() = serialDescriptor<List<Long>>()

	override fun serialize(encoder: Encoder, value: LongRange) {
		encoder.encodeSerializableValue(
			serializer,
			listOf(value.first, value.last)
		)
	}

	override fun deserialize(decoder: Decoder): LongRange {
		return decoder.decodeSerializableValue(serializer).let {
			LongRange(it[0], it[1])
		}
	}
}