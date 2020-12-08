package airlock.types;

// from go, base64.NewEncoding is equivalent to CharsetEncoder / CharsetProvider

public class Auras {
	 public static Aura ATOM = new Aura("") {
		 @Override
		 boolean validate(String representation) {
			 return false;
		 }
	 };   // no aura
	public static Aura D = new Aura("d") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // date
	public static Aura DA = new Aura("da") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // absolute date
	public static Aura DR = new Aura("dr") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // relative date
	public static Aura P = new Aura("p") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // phonemic base (ship name)
	public static Aura R = new Aura("r") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // IEEE floating-point
	public static Aura RD = new Aura("rd") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // double precision  (64 bits)
	public static Aura RH = new Aura("rh") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // half precision (16 bits)
	public static Aura RQ = new Aura("rq") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // quad precision (128 bits)
	public static Aura RS = new Aura("rs") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // single precision (32 bits)
	public static Aura S = new Aura("s") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // signed integer, sign bit low
	public static Aura SB = new Aura("sb") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // signed binary
	public static Aura SD = new Aura("sd") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // signed decimal
	public static Aura SV = new Aura("sv") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // signed base32
	public static Aura SW = new Aura("sw") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // signed base64
	public static Aura SX = new Aura("sx") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // signed hexadecimal
	public static Aura T = new Aura("t") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // UTF-8 text (cord)
	public static Aura TA = new Aura("ta") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // ASCII text (knot)
	public static Aura TAS = new Aura("tas") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};// ASCII text symbol (term)
	public static Aura U = new Aura("u") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // unsigned integer
	public static Aura UB = new Aura("ub") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // unsigned binary
	public static Aura UD = new Aura("ud") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // unsigned decimal
	public static Aura UV = new Aura("uv") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // unsigned base32
	public static Aura UW = new Aura("uw") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};  // unsigned base64

	// unsigned hexadecimal
	public static Aura UX = new Aura("ux") {
		@Override
		boolean validate(String representation) {
			return false;
		}
	};




}
