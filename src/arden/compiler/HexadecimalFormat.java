// arden2bytecode
// Copyright (c) 2017, Klaus-Hendrik Wolf
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

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

final public class HexadecimalFormat extends NumberFormat {
    private boolean upperCase;
    public HexadecimalFormat() {
        super();
        upperCase = false;
    }

    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
        return format((long) number, toAppendTo, pos);
    }

    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos)
    {
        if (upperCase) {
            return new StringBuffer(Long.toHexString(number).toUpperCase());
        } else {
            return new StringBuffer(Long.toHexString(number));
        } 
    }

    public Number parse(String source, ParsePosition parsePosition) {
        return null;
    }

    public void useLowerCase() {
        upperCase = false;
    }

    public void useUpperCase() {
        upperCase = true;
    }
}

