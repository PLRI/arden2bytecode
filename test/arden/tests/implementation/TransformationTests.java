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

package arden.tests.implementation;

import org.junit.Test;

import arden.runtime.ArdenList;
import arden.runtime.ArdenNumber;
import arden.runtime.ArdenValue;

public class TransformationTests extends ExpressionTestBase {
	@Test
	public void minimumFrom() throws Exception {
		assertEval("(11,12)", "MINIMUM 2 FROM (11,14,13,12)");
		assertEval("(,3)", "MINIMUM 2 FROM 3");
		assertEval("null", "MINIMUM 2 FROM (3, \"asdf\")");
		assertEval("()", "MINIMUM 2 FROM ()");
		assertEval("()", "MINIMUM 0 FROM (2,3)");
		assertEval("(1,2,2)", "MINIMUM 3 FROM (3,5,1,2,4,2)");
	}

	@Test
	public void maximumFrom() throws Exception {
		assertEval("(14,13)", "MAXIMUM 2 FROM (11,14,13,12)");
		assertEval("(,3)", "MAXIMUM 2 FROM 3");
		assertEval("null", "MAXIMUM 2 FROM (3, \"asdf\")");
		assertEval("()", "MAXIMUM 2 FROM ()");
		assertEval("()", "MAXIMUM 0 FROM (1,2,3)");
		assertEval("(5,4,4)", "MAXIMUM 3 FROM (1,5,2,4,1,4)");
	}

	@Test
	public void firstFrom() throws Exception {
		assertEval("(11,14)", "FIRST 2 FROM (11,14,13,12)");
		assertEval("(,3)", "FIRST 2 FROM 3");
		assertEval("(null,1)", "FIRST 2 FROM (null,1,2,null)");
		assertEval("()", "FIRST 2 FROM ()");
	}

	@Test
	public void lastFrom() throws Exception {
		assertEval("(13,12)", "LAST 2 FROM (11,14,13,12)");
		assertEval("(,3)", "LAST 2 FROM 3");
		assertEval("(2,null)", "LAST 2 FROM (null,1,2,null)");
		assertEval("()", "LAST 2 FROM ()");
	}

	@Test
	public void increase() throws Exception {
		assertEval("(4,-2,-1)", "INCREASE (11,15,13,12)");
		assertEval("()", "INCREASE 3");
		assertEval("null", "INCREASE ()");
		assertEval("(,1 day)", "INCREASE (1990-03-01,1990-03-02)");
		assertEval("(,1 day)", "INCREASE (1 day, 2 days)");
		assertEval("null", "INCREASE (1990-03-01,1990-03-02,1)");
	}

	@Test
	public void decrease() throws Exception {
		assertEval("(-4,2,1)", "DECREASE (11,15,13,12)");
		assertEval("()", "DECREASE 3");
		assertEval("null", "DECREASE ()");
		assertEval("(,-1 days)", "DECREASE (1990-03-01,1990-03-02)");
		assertEval("(,-1 days)", "DECREASE (1 day, 2 days)");
	}

	@Test
	public void percentIncrease() throws Exception {
		assertEval("\"+36.3636%,-13.3333%\"", "% INCREASE (11,15,13) FORMATTED WITH \"%+.4f%%,%+.4f%%\"");
		assertEval("()", "% INCREASE 3");
		assertEval("null", "% INCREASE ()");
		assertEval("(,100)", "PERCENT INCREASE (1 day, 2 days)");
		assertEval("null", "% INCREASE (1990-03-01,1990-03-02,1)");
	}

	@Test
	public void percentDecrease() throws Exception {
		assertEval("\"-36.3636%,+13.3333%\"", "% DECREASE (11,15,13) FORMATTED WITH \"%+.4f%%,%+.4f%%\"");
		assertEval("()", "% DECREASE 3");
		assertEval("null", "% DECREASE ()");
		assertEval("(,-100)", "PERCENT DECREASE (1 day, 2 days)");
	}

	@Test
	public void earliestFrom() throws Exception {
		assertEval("()", "EARLIEST 2 FROM ()");
		assertEval("null", "EARLIEST 2 FROM (1,2)");
	}

	@Test
	public void latestFrom() throws Exception {
		assertEval("()", "LATEST 2 FROM ()");
		assertEval("null", "LATEST 2 FROM (1,2)");
	}

	@Test
	public void indexMinimumFrom() throws Exception {
		assertEval("(1,4)", "INDEX MINIMUM 2 FROM (11,14,13,12)");
		assertEval("(3,4,6)", "INDEX MINIMUM 3 FROM (3,5,1,2,4,2)");
		assertEval("null", "INDEX MIN 2 FROM (3, \"asdf\")");
		assertEval("(,1)", "INDEX MINIMUM 2 FROM 3");
		assertEval("()", "INDEX MINIMUM 0 FROM (2,3)");
	}

	@Test
	public void indexMaximumFrom() throws Exception {
		assertEval("(2,3)", "INDEX MAXIMUM 2 FROM (11,14,13,12)");
		assertEval("(1,2,5)", "INDEX MAXIMUM 3 FROM (3,5,1,2,4,2)");
		assertEval("null", "INDEX MAX 2 FROM (3, \"asdf\")");
		assertEval("(,1)", "INDEX MAXIMUM 2 FROM 3");
		assertEval("()", "INDEX MAXIMUM 0 FROM (2,3)");
	}

	@Test
	public void indexEarliestFrom() throws Exception {
		assertEval("()", "EARLIEST 2 FROM ()");
		assertEval("null", "EARLIEST 2 FROM (1,2)");
	}

	@Test
	public void indexLatestFrom() throws Exception {
		assertEval("()", "LATEST 2 FROM ()");
		assertEval("null", "LATEST 2 FROM (1,2)");
	}

	@Test
	public void interval() throws Exception {
		assertEval("null", "INTERVAL (3,4)");
		assertEval("null", "INTERVAL 3");
		assertEval("null", "INTERVAL ()");
		assertEval("null", "INTERVAL (1990-03-01,1990-03-02)");

		ArdenValue[] arg = { ArdenNumber.create(1, 1000), ArdenNumber.create(5, 1500), ArdenNumber.create(100, 500) };
		assertEvalWithArgument("(0.5 seconds,-1 seconds)", "INTERVAL arg", new ArdenList(arg), new TestContext());
	}
}
