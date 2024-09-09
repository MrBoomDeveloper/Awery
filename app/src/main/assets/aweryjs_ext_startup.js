const console = globalThis.console;

// Copy the reference to be able use it later
const __AWERY_BINDING_COPY__ = __AWERY_BINDING__;

const Awery = {
    ...__AWERY_BINDING__,
	...__AWERY_CONSTANTS__,
	...__AWERY_GLOBALS__,

	get storage() {
	    return __AWERY_BINDING_COPY__.storage;
	}
}