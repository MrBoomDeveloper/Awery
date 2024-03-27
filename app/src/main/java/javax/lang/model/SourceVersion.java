package javax.lang.model;

/**
 * Ignore any warning that the package exists in another module.
 * Another doesn't have such classes so i did added them by myself.
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

	private SourceVersion() {}

	public static SourceVersion latest() {
		return RELEASE_17;
	}

	private static SourceVersion getLatestSupported() {
		return RELEASE_17;
	}

	public static SourceVersion latestSupported() {
		return latestSupported;
	}

	public static boolean isIdentifier(CharSequence name) {
		String id = name.toString();
		if (id.length() == 0) {
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
		int var4 = var3.length;

		for(int var5 = 0; var5 < var4; ++var5) {
			String s = var3[var5];
			if (!isIdentifier(s) || isKeyword(s, version)) {
				return false;
			}
		}

		return true;
	}

	public static boolean isKeyword(CharSequence s) {
		return isKeyword(s, latest());
	}

	public static boolean isKeyword(CharSequence s, SourceVersion version) {
		switch (s.toString()) {
			case "strictfp":
				return version.compareTo(RELEASE_2) >= 0;
			case "assert":
				return version.compareTo(RELEASE_4) >= 0;
			case "enum":
				return version.compareTo(RELEASE_5) >= 0;
			case "_":
				return version.compareTo(RELEASE_9) >= 0;
			case "public":
			case "protected":
			case "private":
			case "abstract":
			case "static":
			case "final":
			case "transient":
			case "volatile":
			case "synchronized":
			case "native":
			case "class":
			case "interface":
			case "extends":
			case "package":
			case "throws":
			case "implements":
			case "boolean":
			case "byte":
			case "char":
			case "short":
			case "int":
			case "long":
			case "float":
			case "double":
			case "void":
			case "if":
			case "else":
			case "try":
			case "catch":
			case "finally":
			case "do":
			case "while":
			case "for":
			case "continue":
			case "switch":
			case "case":
			case "default":
			case "break":
			case "throw":
			case "return":
			case "this":
			case "new":
			case "super":
			case "import":
			case "instanceof":
			case "goto":
			case "const":
			case "null":
			case "true":
			case "false":
				return true;
			default:
				return false;
		}
	}
}