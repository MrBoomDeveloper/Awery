package javax.lang.model;

/**
 * Ignore any warning that the package exists in another module.
 * The real one doesn't have such classes so i did added them myself.
 */
public enum SourceVersion {
	RELEASE_0,
	RELEASE_1,
	RELEASE_2,
	RELEASE_3,
	RELEASE_4,
	RELEASE_5,
	RELEASE_6,
	RELEASE_7,
	RELEASE_8,
	RELEASE_9,
	RELEASE_10,
	RELEASE_11,
	RELEASE_12,
	RELEASE_13,
	RELEASE_14,
	RELEASE_15,
	RELEASE_16,
	RELEASE_17;

	private static final SourceVersion latestSupported = getLatestSupported();

	public static SourceVersion latest() {
		return RELEASE_7;
	}

	private static SourceVersion getLatestSupported() {
		return RELEASE_7;
	}

	public static SourceVersion latestSupported() {
		return latestSupported;
	}

	public static boolean isIdentifier(CharSequence name) {
		String id = name.toString();
		if(id.isEmpty()) {
			return false;
		} else {
			int cp = id.codePointAt(0);
			if (!Character.isJavaIdentifierStart(cp)) {
				return false;
			} else {
				for(int i = Character.charCount(cp); i < id.length(); i += Character.charCount(cp)) {
					cp = id.codePointAt(i);
					if (!Character.isJavaIdentifierPart(cp)) {
						return false;
					}
				}

				return true;
			}
		}
	}

	public static boolean isName(CharSequence name) {
		return isName(name, latest());
	}

	public static boolean isName(CharSequence name, SourceVersion version) {
		String id = name.toString();
		String[] var3 = id.split("\\.", -1);

		for(String s : var3) {
			if(!isIdentifier(s) || isKeyword(s, version)) {
				return false;
			}
		}

		return true;
	}

	public static boolean isKeyword(CharSequence s) {
		return isKeyword(s, latest());
	}

	public static boolean isKeyword(CharSequence s, SourceVersion version) {
		return switch(s.toString()) {
			case "strictfp" -> version.compareTo(RELEASE_2) >= 0;
			case "assert" -> version.compareTo(RELEASE_4) >= 0;
			case "enum" -> version.compareTo(RELEASE_5) >= 0;
			case "_" -> version.compareTo(RELEASE_9) >= 0;
			case "public", "protected", "private", "abstract", "static", "final", "transient", "volatile", "synchronized",
				 "native", "class", "interface", "extends", "package", "throws", "implements", "boolean", "byte", "char",
				 "short", "int", "long", "float", "double", "void", "if", "else", "try", "catch", "finally", "do", "while",
				 "for", "continue", "switch", "case", "default", "break", "throw", "return", "this", "new", "super",
				 "import", "instanceof", "goto", "const", "null", "true", "false" -> true;
			default -> false;
		};
	}
}