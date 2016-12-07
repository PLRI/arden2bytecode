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

public class ArdenObject extends ArdenValue {
	public final ObjectType type;
	public final ArdenValue[] fields;

	public ArdenObject(ObjectType type) {
		this.type = type;
		this.fields = new ArdenValue[type.fieldNames.length];
		for (int i = 0; i < fields.length; i++)
			fields[i] = ArdenNull.INSTANCE;
	}

	@Override
	public ArdenValue setTime(long newPrimaryTime) {
		for (int i = 0; i < fields.length; i++)
			fields[i] = fields[i].setTime(newPrimaryTime);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("NEW ");
		b.append(type.name);
		if (fields.length > 0) {
			b.append(" WITH [");
			for (int i = 0; i < fields.length; i++) {
				if (i > 0)
					b.append(", ");
				b.append(type.fieldNames[i].toString());
				b.append(":=");
				b.append(fields[i].toString());
			}
			b.append("]");
		}
		return b.toString();
	}
}
