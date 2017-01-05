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

package arden.compiler;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import arden.codegenerator.FieldReference;
import arden.codegenerator.Label;
import arden.codegenerator.MethodWriter;
import arden.compiler.node.Token;
import arden.runtime.ArdenList;
import arden.runtime.ArdenNull;
import arden.runtime.ArdenString;
import arden.runtime.ArdenValue;

/**
 * This class is responsible for compiling 'FORMATTED WITH' operator
 * invocations.
 * 
 * @author Daniel Grunwald
 * 
 */
final class FormattingCompiler {
	private final String controlString;
	private final Token locationForParseErrors;

	public FormattingCompiler(String controlString, Token locationForParseErrors) {
		this.controlString = controlString;
		this.locationForParseErrors = locationForParseErrors;
	}

	private CompilerContext context;
	private int valuesVariable;

	/**
	 * Creates code that pops an ArdenValue (the input data) from the stack and
	 * pushes an ArdenString (the formatted data).
	 */
	public void run(CompilerContext context) {
		// we'll emit code in the form:
		// ArdenValue[] values = ExpressionHelpers.unaryComma(inputData).values;
		// new ArdenString(new StringBuilder().append(...).toString())

		this.context = context;
		try {
			// convert ArdenValue on stack to ArdenList
			context.writer.invokeStatic(ExpressionCompiler.getMethod("unaryComma", ArdenValue.class));
			// fetch the values inside the list
			context.writer.loadInstanceField(ArdenList.class.getField("values"));
			// store the ArdenValue[] in a variable
			this.valuesVariable = context.allocateVariable();
			context.writer.storeVariable(valuesVariable);

			// new ArdenString(...)
			context.writer.newObject(ArdenString.class);
			context.writer.dup();
			// new StringBuilder()
			context.writer.newObject(StringBuilder.class);
			context.writer.dup();
			context.writer.invokeConstructor(StringBuilder.class.getConstructor());

			compileAppendCalls();

			context.writer.invokeInstance(StringBuilder.class.getMethod("toString"));
			context.writer.invokeConstructor(ArdenString.class.getConstructor(String.class));
		} catch (NoSuchFieldException ex) {
			throw new RuntimeException(ex);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void compileAppendCalls() throws NoSuchMethodException, NoSuchFieldException {
		StringBuilder currentLiteralOutput = new StringBuilder();
		int dataIndex = 0; // position in consumed data input array
		for (int pos = 0; pos < controlString.length(); pos++) {
			char c = controlString.charAt(pos);
			if (c == '%') {
				if (pos + 1 < controlString.length() && controlString.charAt(pos + 1) == '%') {
					currentLiteralOutput.append('%');
					pos++; // skip the second %
				} else {
					if (currentLiteralOutput.length() > 0) {
						context.writer.loadStringConstant(currentLiteralOutput.toString());
						context.writer.invokeInstance(StringBuilder.class.getMethod("append", String.class));
						currentLiteralOutput.setLength(0);
					}
					FormatSpecification spec = parse(pos);
					int widthDataElementIndex = -1;
					if (spec.width < 0) {
						widthDataElementIndex = dataIndex++;
					}
					compileToStringCallForData(spec, dataIndex++);
					compilePaddingCall(spec, widthDataElementIndex);
					context.writer.invokeInstance(StringBuilder.class.getMethod("append", String.class));
					pos = spec.endPosition;
				}
			} else {
				currentLiteralOutput.append(c);
			}
		}
		if (currentLiteralOutput.length() > 0) {
			context.writer.loadStringConstant(currentLiteralOutput.toString());
			context.writer.invokeInstance(StringBuilder.class.getMethod("append", String.class));
		}
	}

	private void compileToStringCallForData(FormatSpecification spec, int dataElementIndex)
			throws NoSuchFieldException, NoSuchMethodException {
		loadDataElement(dataElementIndex);
		switch (spec.type) {
		case 'c':
		case 'C':
			// The number is assumed to represent a character code
			// to be output as a character.
			context.writer.invokeStatic(Compiler.getRuntimeHelper("formatCharacter", ArdenValue.class));
			break;
		case 'S': // string as string with length limit
		case 's': // lowercase not specified in A5.1
			context.writer.invokeStatic(ExpressionCompiler.getMethod("toString", ArdenValue.class));
			if (spec.precision > 0) {
				context.writer.loadIntegerConstant(spec.precision);
				context.writer
						.invokeStatic(Compiler.getRuntimeHelper("limitStringLength", String.class, int.class));
			}
			break;
		case 'D': // signed decimal integer
		case 'd': // lowercase not specified in A5.1
		case 'I': // signed decimal integer
		case 'i': // lowercase not specified in A5.1
		case 'U': // unsigned decimal integer
		case 'u': // lowercase not specified in A5.1
        case 'e':
        case 'E':
		case 'F': // number - signed value [-]dddd.dddd
		case 'f': // lowercase not specified in A5.1
		case 'g': // TODO: implement %g: double use %f or %e, whichever is more compact
        case 'G': // TODO: implement %G: identical to %e, except E, rather then e
			FieldReference formatField = context.codeGenerator.createStaticFinalField(DecimalFormat.class);
			MethodWriter init = context.codeGenerator.getStaticInitializer();
			// format = new DecimalFormat();
			init.newObject(DecimalFormat.class);
			init.dup();
			init.invokeConstructor(DecimalFormat.class.getConstructor());
			init.storeStaticField(formatField);

			// format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
			init.loadStaticField(formatField);
			init.loadStaticField(Locale.class.getField("ENGLISH"));
			init.invokeStatic(DecimalFormatSymbols.class.getMethod("getInstance", Locale.class));
			init.invokeInstance(DecimalFormat.class.getMethod("setDecimalFormatSymbols", DecimalFormatSymbols.class));

			init.loadStaticField(formatField);
			init.loadIntegerConstant(0);
			init.invokeInstance(DecimalFormat.class.getMethod("setGroupingUsed", boolean.class));

            switch (spec.type) {
            case 'd':
            case 'D':
            case 'i':
            case 'I':
            case 'u':
            case 'U':
				// format.setMaximumFractionDigits(0);
				init.loadStaticField(formatField);
				init.loadIntegerConstant(0);
				init.invokeInstance(DecimalFormat.class.getMethod("setMaximumFractionDigits", int.class));

				if (spec.precision > 1) {
					init.loadStaticField(formatField);
					init.loadIntegerConstant(spec.precision);
					init.invokeInstance(DecimalFormat.class.getMethod("setMinimumIntegerDigits", int.class));
				}
                break;
            case 'e':
            case 'E':
            case 'f':
            case 'F':
            case 'g': // TODO: use %e, when more compact
            case 'G': // TODO: use %E, when more compact
                if (spec.type == 'e' || spec.type == 'E') {
                    init.loadStaticField(formatField);
                    init.loadStringConstant("0.######E000");
                    init.invokeInstance(DecimalFormat.class.getMethod("applyPattern", String.class));
                }
                init.loadStaticField(formatField);
                init.loadIntegerConstant(spec.precision < 0 ? 6 : spec.precision);
                init.invokeInstance(DecimalFormat.class.getMethod("setMaximumFractionDigits", int.class));

                init.loadStaticField(formatField);
                init.loadIntegerConstant(spec.precision < 0 ? 6 : spec.precision);
                init.invokeInstance(DecimalFormat.class.getMethod("setMinimumFractionDigits", int.class));

                if (spec.numberSignFlag) {
                        // TODO: implement this
                }
                
                break;
            default:
                throw new RuntimeException("Format specifier %" + spec.type + " not implemented");
			}

			if (spec.plusFlag) {
				init.loadStaticField(formatField);
				init.loadStringConstant("+");
				init.invokeInstance(DecimalFormat.class.getMethod("setPositivePrefix", String.class));
			} else if (spec.spaceFlag) {
				init.loadStaticField(formatField);
				init.loadStringConstant(" ");
				init.invokeInstance(DecimalFormat.class.getMethod("setPositivePrefix", String.class));
			}

			context.writer.loadStaticField(formatField);
			context.writer
					.invokeStatic(Compiler.getRuntimeHelper("formatNumber", ArdenValue.class, NumberFormat.class));
            // add the required '+' to the exponent
            if (spec.type == 'e' || spec.type == 'g'
                || spec.type == 'E' || spec.type == 'G') {
                // resultString.replace("E", "E+")
				context.writer.loadStringConstant("E");
				context.writer.loadStringConstant("E+");
                context.writer.invokeInstance(String.class.getMethod("replace", CharSequence.class, CharSequence.class));
                // resultString.replace("+-", "-")
				context.writer.loadStringConstant("+-");
				context.writer.loadStringConstant("-");
                context.writer.invokeInstance(String.class.getMethod("replace", CharSequence.class, CharSequence.class));
                // make exponent lower case if desired
                if (spec.type == 'e' || spec.type == 'g') {
                    context.writer.invokeInstance(String.class.getMethod("toLowerCase"));
                }
            }
			break;
		case 'T': // time is based on environment settings and the precision value
		case 't': // lowercase not specified in A5.1
			context.writer.loadIntegerConstant(spec.precision < 0 ? 5 : spec.precision);
			context.writer.invokeStatic(Compiler.getRuntimeHelper("formatTime", ArdenValue.class, int.class));
			break;
		
        case 'x': // %x: number - unsigned hexadecimal integer using "abcdef"
        case 'X': // %X: number - unsigned hexadecimal integer using "ABCDEF"
			FieldReference formatField2 = context.codeGenerator.createStaticFinalField(HexadecimalFormat.class);
			MethodWriter init2 = context.codeGenerator.getStaticInitializer();
			// format = new HexadecimalFormat();
			init2.newObject(HexadecimalFormat.class);
			init2.dup();
			init2.invokeConstructor(HexadecimalFormat.class.getConstructor());
			init2.dup();
			init2.storeStaticField(formatField2);
            if (spec.type == 'X') {
                init2.invokeInstance(HexadecimalFormat.class.getMethod("useUpperCase"));
            }

			context.writer.loadStaticField(formatField2);
			context.writer
					.invokeStatic(Compiler.getRuntimeHelper("formatNumber", ArdenValue.class, NumberFormat.class));
			break;
		case 'O': // %O: number - unsigned octal integer
		case 'o': // lowercase not specified in A5.1
			FieldReference formatField3 = context.codeGenerator.createStaticFinalField(OctalFormat.class);
			MethodWriter init3 = context.codeGenerator.getStaticInitializer();
			// format = new OctalFormat();
			init3.newObject(OctalFormat.class);
			init3.dup();
			init3.invokeConstructor(OctalFormat.class.getConstructor());
			init3.storeStaticField(formatField3);

			context.writer.loadStaticField(formatField3);
			context.writer
					.invokeStatic(Compiler.getRuntimeHelper("formatNumber", ArdenValue.class, NumberFormat.class));
			break;
		default:
			throw new RuntimeCompilerException(locationForParseErrors, "Unknown formatting specification: '%"
					+ spec.type + "'");
		}
	}

	private void compilePaddingCall(FormatSpecification spec, int widthDataElementIndex) throws SecurityException,
			NoSuchFieldException {
		if (spec.width != 0) {
			if (spec.width < 0) {
				loadDataElement(widthDataElementIndex);
				context.writer.invokeStatic(Compiler.getRuntimeHelper("getPrimitiveIntegerValue", ArdenValue.class));
			} else {
				context.writer.loadIntegerConstant(spec.width);
			}
			if (spec.minusFlag) {
				// left align
				context.writer.loadIntegerConstant(' ');
				context.writer.invokeStatic(Compiler.getRuntimeHelper("padRight", String.class, int.class, char.class));
			} else {
				// right align
				if (spec.zeroFlag) {
					context.writer.loadIntegerConstant('0');
				} else {
					context.writer.loadIntegerConstant(' ');
				}
				context.writer.invokeStatic(Compiler.getRuntimeHelper("padLeft", String.class, int.class, char.class));
			}
		}
	}

	private void loadDataElement(int dataElementIndex) throws SecurityException, NoSuchFieldException {
		// emit: (dataElementIndex < values.length) ? values[i] :
		// ArdenNull.INSTANCE
		Label trueLabel = new Label();
		Label endLabel = new Label();
		context.writer.loadIntegerConstant(dataElementIndex);
		context.writer.loadVariable(valuesVariable);
		context.writer.arrayLength();
		context.writer.jumpIfLessThan(trueLabel);
		// false:
		context.writer.loadStaticField(ArdenNull.class.getField("INSTANCE"));
		context.writer.jump(endLabel);
		// true:
		context.writer.markForwardJumpsOnly(trueLabel);
		context.writer.loadVariable(valuesVariable);
		context.writer.loadIntegerConstant(dataElementIndex);
		context.writer.loadObjectFromArray();

		context.writer.markForwardJumpsOnly(endLabel);
	}

	// syntax of format specifications: %[flags][width][.precision]type
	private static class FormatSpecification {
		boolean minusFlag, plusFlag, zeroFlag, spaceFlag, numberSignFlag;
		int width; // minimum width. 0 = not specified, -1 = read from input
		// data
		int precision; // precision: -1 = not specified
		char type;
		int endPosition; // the position of the type character (=last character
		// in spec)
	}

	private FormatSpecification parse(int startPos) {
		assert controlString.charAt(startPos) == '%';
		FormatSpecification spec = new FormatSpecification();
		int pos = startPos + 1;
		// read flags:
		for (; pos < controlString.length(); pos++) {
			char c = controlString.charAt(pos);
			if (c == '-')
				spec.minusFlag = true;
			else if (c == '+')
				spec.plusFlag = true;
			else if (c == '0')
				spec.zeroFlag = true;
			else if (c == ' ')
				spec.spaceFlag = true;
			else if (c == '#')
				spec.numberSignFlag = true;
			else
				break; // end of flags
		}
		// read width:
		for (; pos < controlString.length(); pos++) {
			char c = controlString.charAt(pos);
			if (c >= '0' && c <= '9') {
				spec.width *= 10;
				spec.width += (c - '0');
			} else if (c == '*') {
				if (spec.width != 0)
					throw new RuntimeCompilerException(locationForParseErrors, "Cannot specify both width and '*'.");
				spec.width = -1;
				pos++;
				break;
			} else {
				break; // end of width
			}
		}
		// read precision:
		if (pos < controlString.length() && controlString.charAt(pos) == '.') {
			pos++;
			spec.precision = 0;
			for (; pos < controlString.length(); pos++) {
				char c = controlString.charAt(pos);
				if (c >= '0' && c <= '9') {
					spec.precision *= 10;
					spec.precision += (c - '0');
				} else {
					break; // end of precision
				}
			}
		} else {
			spec.precision = -1;
		}
		if (pos < controlString.length()) {
			spec.type = controlString.charAt(pos);
			spec.endPosition = pos;
			return spec;
		} else {
			throw new RuntimeCompilerException(locationForParseErrors, "Unexpected end of format specification");
		}
	}
}
