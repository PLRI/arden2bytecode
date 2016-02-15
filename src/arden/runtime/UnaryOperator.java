// arden2bytecode
// Copyright (c) 2010, Daniel Grunwald
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// - Redistributions of source code must retain the above copyright notice, this list
//   of conditions and the following disclaimer.
//
// - Redistributions in binary form must reproduce the above copyright notice, this list
//   of conditions and the following disclaimer in the documentation and/or other materials
//   provided with the distribution.
//
// - Neither the name of the owner nor the names of its contributors may be used to
//   endorse or promote products derived from this software without specific prior written
//   permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS &AS IS& AND ANY EXPRESS
// OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
// AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
// IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package arden.runtime;

/** Unary operators of the form '<n:type> := op <n:type>' */
public abstract class UnaryOperator {
	private final String name;

	public abstract ArdenValue runElement(ArdenValue val);

	public static final UnaryOperator NOT = new UnaryOperator("NOT") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val instanceof ArdenBoolean)
				return ArdenBoolean.create(!((ArdenBoolean) val).value, val.primaryTime);
			else
				return ArdenNull.create(val.primaryTime);
		};
	};

	public static final UnaryOperator PLUS = new UnaryOperator("PLUS") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val instanceof ArdenNumber || val instanceof ArdenDuration)
				return val;
			else
				return ArdenNull.create(val.primaryTime);
		};
	};

	public static final UnaryOperator MINUS = new UnaryOperator("MINUS") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val instanceof ArdenNumber) {
				return ArdenNumber.create(-((ArdenNumber) val).value, val.primaryTime);
			} else if (val instanceof ArdenDuration) {
				ArdenDuration dur = (ArdenDuration) val;
				return ArdenDuration.create(-dur.value, dur.isMonths, dur.primaryTime);
			} else {
				return ArdenNull.create(val.primaryTime);
			}
		};
	};

	public static final UnaryOperator EXTRACTSECOND = new UnaryOperator("EXTRACTSECOND") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val instanceof ArdenTime) {
				long ms = ((ArdenTime) val).value % 60000;
				return ArdenNumber.create(ms / 1000.0, val.primaryTime);
			} else {
				return ArdenNull.create(val.primaryTime);
			}
		};
	};

	public static final UnaryOperator TIME = new UnaryOperator("TIME") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			if(val instanceof ArdenObject) {
				// check primary time of fields
				ArdenValue[] fields = ((ArdenObject) val).fields;
				if(fields.length == 0) {
					return ArdenNull.INSTANCE;
				}
				long firstTime = fields[0].primaryTime;
				for(ArdenValue field : fields) {
					if (field instanceof ArdenList || field.primaryTime == ArdenValue.NOPRIMARYTIME
							|| field.primaryTime != firstTime) {
						return ArdenNull.INSTANCE;
					}
				}
				return new ArdenTime(firstTime);
			} else if (val.primaryTime == ArdenValue.NOPRIMARYTIME)
				return ArdenNull.INSTANCE;
			else
				return new ArdenTime(val.primaryTime, val.primaryTime);
		};
	};

	public static final UnaryOperator ISNULL = new UnaryOperator("ISNULL") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			return ArdenBoolean.create(val instanceof ArdenNull, val.primaryTime);
		};
	};

	public static final UnaryOperator ISBOOLEAN = new UnaryOperator("ISBOOLEAN") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			return ArdenBoolean.create(val instanceof ArdenBoolean, val.primaryTime);
		};
	};

	public static final UnaryOperator ISNUMBER = new UnaryOperator("ISNUMBER") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			return ArdenBoolean.create(val instanceof ArdenNumber, val.primaryTime);
		};
	};

	public static final UnaryOperator ISTIME = new UnaryOperator("ISTIME") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			return ArdenBoolean.create(val instanceof ArdenTime, val.primaryTime);
		};
	};

	public static final UnaryOperator ISDURATION = new UnaryOperator("ISDURATION") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			return ArdenBoolean.create(val instanceof ArdenDuration, val.primaryTime);
		};
	};

	public static final UnaryOperator ISSTRING = new UnaryOperator("ISSTRING") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			return ArdenBoolean.create(val instanceof ArdenString, val.primaryTime);
		};
	};

	public static final UnaryOperator ISOBJECT = new UnaryOperator("ISOBJECT") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			return ArdenBoolean.create(val instanceof ArdenObject, val.primaryTime);
		};
	};

	public static final UnaryOperator ABS = new NumericUnaryOperator("ABS") {
		@Override
		public double runNumber(double input) {
			return Math.abs(input);
		}
	};

	public static final UnaryOperator SQRT = new NumericUnaryOperator("SQRT") {
		@Override
		public double runNumber(double input) {
			return Math.sqrt(input);
		}
	};

	public static final UnaryOperator LOG = new NumericUnaryOperator("LOG") {
		@Override
		public double runNumber(double input) {
			return Math.log(input);
		}
	};

	public static final UnaryOperator LOG10 = new NumericUnaryOperator("LOG10") {
		@Override
		public double runNumber(double input) {
			return Math.log10(input);
		}
	};

	public static final UnaryOperator ARCCOS = new NumericUnaryOperator("ARCCOS") {
		@Override
		public double runNumber(double input) {
			return Math.acos(input);
		}
	};

	public static final UnaryOperator ARCSIN = new NumericUnaryOperator("ARCSIN") {
		@Override
		public double runNumber(double input) {
			return Math.asin(input);
		}
	};

	public static final UnaryOperator ARCTAN = new NumericUnaryOperator("ARCTAN") {
		@Override
		public double runNumber(double input) {
			return Math.atan(input);
		}
	};

	public static final UnaryOperator COS = new NumericUnaryOperator("COS") {
		@Override
		public double runNumber(double input) {
			return Math.cos(input);
		}
	};

	public static final UnaryOperator SIN = new NumericUnaryOperator("SIN") {
		@Override
		public double runNumber(double input) {
			return Math.sin(input);
		}
	};

	public static final UnaryOperator TAN = new NumericUnaryOperator("TAN") {
		@Override
		public double runNumber(double input) {
			return Math.tan(input);
		}
	};

	public static final UnaryOperator EXP = new NumericUnaryOperator("EXP") {
		@Override
		public double runNumber(double input) {
			return Math.exp(input);
		}
	};

	public static final UnaryOperator FLOOR = new NumericUnaryOperator("FLOOR") {
		@Override
		public double runNumber(double input) {
			return Math.floor(input);
		}
	};

	public static final UnaryOperator CEILING = new NumericUnaryOperator("CEILING") {
		@Override
		public double runNumber(double input) {
			return Math.ceil(input);
		}
	};

	public static final UnaryOperator ROUND = new NumericUnaryOperator("ROUND") {
		@Override
		public double runNumber(double input) {
			return input < 0 ? Math.ceil(input - 0.5) : Math.floor(input + 0.5);
		}
	};

	public static final UnaryOperator TRUNCATE = new NumericUnaryOperator("TRUNCATE") {
		@Override
		public double runNumber(double input) {
			return input < 0 ? Math.ceil(input) : Math.floor(input);
		}
	};

	public static final UnaryOperator ASNUMBER = new UnaryOperator("ASNUMBER") {
		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val instanceof ArdenBoolean)
				return ArdenNumber.create(((ArdenBoolean) val).value ? 1 : 0, val.primaryTime);
			else if (val instanceof ArdenNumber)
				return val;
			else if (val instanceof ArdenString) {
				double d;
				try {
					d = Double.parseDouble(((ArdenString) val).value);
				} catch (NumberFormatException e) {
					d = Double.NaN;
				}
				return ArdenNumber.create(d, val.primaryTime);
			} else
				return ArdenNull.create(val.primaryTime);
		};
	};

	public UnaryOperator(String name) {
		this.name = name;
	}

	/** Implements the list logic for running the operator. */
	public final ArdenValue run(ArdenValue val) {
		if (val instanceof ArdenList) {
			ArdenList inputList = (ArdenList) val;
			ArdenValue[] results = new ArdenValue[inputList.values.length];
			for (int i = 0; i < results.length; i++) {
				results[i] = runElement(inputList.values[i]);
			}
			return new ArdenList(results);
		} else {
			return runElement(val);
		}
	}

	@Override
	public String toString() {
		return name;
	}

	private static abstract class NumericUnaryOperator extends UnaryOperator {
		public NumericUnaryOperator(String name) {
			super(name);
		}

		public abstract double runNumber(double input);

		@Override
		public ArdenValue runElement(ArdenValue val) {
			if (val instanceof ArdenNumber) {
				return ArdenNumber.create(runNumber(((ArdenNumber) val).value), val.primaryTime);
			} else {
				return ArdenNull.create(val.primaryTime);
			}
		}
	}
}
