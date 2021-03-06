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

/**
 * This class is used to dynamically construct database queries.
 * 
 * The base class provides default implementations for all methods but
 * execute(). These work by calling execute() and then processing the
 * constraints/aggregation on the results using a MemoryQuery.
 * 
 * @author Daniel Grunwald
 * 
 */
public abstract class DatabaseQuery {
	/** The database query that always returns an empty result (0 columns) */
	public static final DatabaseQuery NULL = new MemoryQuery(ArdenList.EMPTY.values);

	/**
	 * Executes the query.
	 * 
	 * @return Returns an array containing the result values. Every entry in the
	 *         array represents one column of the result. If the result set has
	 *         multiple rows, the array entries will be ArdenLists..
	 * 
	 */
	public abstract ArdenValue[] execute();

	/**
	 * Filters the query results to those results that occur within the
	 * specified time range.
	 */
	public DatabaseQuery occursWithinTo(ArdenTime start, ArdenTime end) {
		return new MemoryQuery(execute()).occursWithinTo(start, end);
	}

	/**
	 * Filters the query results to those results that do not occur within the
	 * specified time range.
	 */
	public DatabaseQuery occursNotWithinTo(ArdenTime start, ArdenTime end) {
		return new MemoryQuery(execute()).occursNotWithinTo(start, end);
	}

	/**
	 * Filters the query results to those results that occur before the
	 * specified point in time.
	 */
	public DatabaseQuery occursBefore(ArdenTime time) {
		return new MemoryQuery(execute()).occursBefore(time);
	}

	/**
	 * Filters the query results to those results that do not occur before the
	 * specified point in time.
	 */
	public DatabaseQuery occursNotBefore(ArdenTime time) {
		return new MemoryQuery(execute()).occursNotBefore(time);
	}

	/**
	 * Filters the query results to those results that occur after the specified
	 * point in time.
	 */
	public DatabaseQuery occursAfter(ArdenTime time) {
		return new MemoryQuery(execute()).occursAfter(time);
	}

	/**
	 * Filters the query results to those results that occur after the specified
	 * point in time.
	 */
	public DatabaseQuery occursNotAfter(ArdenTime time) {
		return new MemoryQuery(execute()).occursNotAfter(time);
	}

	/**
	 * Filters the query results to those results that occur at the specified
	 * point in time.
	 */
	public DatabaseQuery occursAt(ArdenTime time) {
		return new MemoryQuery(execute()).occursAt(time);
	}

	/**
	 * Filters the query results to those results that do not occur at the
	 * specified point in time.
	 */
	public DatabaseQuery occursNotAt(ArdenTime time) {
		return new MemoryQuery(execute()).occursNotAt(time);
	}

	/**
	 * 'average' aggregation operator.
	 */
	public DatabaseQuery average() {
		return new MemoryQuery(execute()).average();
	}

	/**
	 * 'count' aggregation operator.
	 */
	public DatabaseQuery count() {
		return new MemoryQuery(execute()).count();
	}

	/**
	 * 'exists' aggregation operator.
	 */
	public DatabaseQuery exist() {
		return new MemoryQuery(execute()).exist();
	}

	/**
	 * 'sum' aggregation operator.
	 */
	public DatabaseQuery sum() {
		return new MemoryQuery(execute()).sum();
	}

	/**
	 * 'median' aggregation operator.
	 */
	public DatabaseQuery median() {
		return new MemoryQuery(execute()).median();
	}

	/**
	 * 'minimum' aggregation operator.
	 */
	public DatabaseQuery minimum() {
		return new MemoryQuery(execute()).minimum();
	}

	/**
	 * 'minimum' transformation operator.
	 */
	public DatabaseQuery minimum(int numberOfElements) {
		return new MemoryQuery(execute()).minimum(numberOfElements);
	}

	/**
	 * 'maximum' aggregation operator.
	 */
	public DatabaseQuery maximum() {
		return new MemoryQuery(execute()).maximum();
	}

	/**
	 * 'maximum' transformation operator.
	 */
	public DatabaseQuery maximum(int numberOfElements) {
		return new MemoryQuery(execute()).maximum(numberOfElements);
	}

	/**
	 * 'last' aggregation operator.
	 */
	public DatabaseQuery last() {
		return new MemoryQuery(execute()).last();
	}

	/**
	 * 'last' transformation operator.
	 */
	public DatabaseQuery last(int numberOfElements) {
		return new MemoryQuery(execute()).last(numberOfElements);
	}

	/**
	 * 'first' aggregation operator.
	 */
	public DatabaseQuery first() {
		return new MemoryQuery(execute()).first();
	}

	/**
	 * 'first' transformation operator.
	 */
	public DatabaseQuery first(int numberOfElements) {
		return new MemoryQuery(execute()).first(numberOfElements);
	}

	/**
	 * 'latest' aggregation operator.
	 */
	public DatabaseQuery latest() {
		return new MemoryQuery(execute()).latest();
	}

	/**
	 * 'latest' transformation operator.
	 */
	public DatabaseQuery latest(int numberOfElements) {
		return new MemoryQuery(execute()).latest(numberOfElements);
	}

	/**
	 * 'earliest' aggregation operator.
	 */
	public DatabaseQuery earliest() {
		return new MemoryQuery(execute()).earliest();
	}

	/**
	 * 'earliest' transformation operator.
	 */
	public DatabaseQuery earliest(int numberOfElements) {
		return new MemoryQuery(execute()).earliest(numberOfElements);
	}
}
