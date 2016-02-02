package arden.tests.specification.testcompiler.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import arden.compiler.CompiledMlm;
import arden.compiler.CompilerException;
import arden.runtime.ArdenValue;
import arden.tests.specification.testcompiler.TestCompiler;
import arden.tests.specification.testcompiler.TestCompilerCompiletimeException;
import arden.tests.specification.testcompiler.TestCompilerException;
import arden.tests.specification.testcompiler.TestCompilerResult;
import arden.tests.specification.testcompiler.TestCompilerRuntimeException;

public class TestCompilerImpl implements TestCompiler {
	
	private arden.compiler.Compiler compiler = new arden.compiler.Compiler();
	
	@Override
	public String getTestInterfaceMapping() {
		return TestContext.INTERFACE_MAPPING;
	}

	@Override
	public String getTestEventMapping() {
		return TestContext.EVENT_MAPPING;
	}
	
	@Override
	public String getTestMessageMapping() {
		return TestContext.MESSAGE_MAPPING;
	}

	@Override
	public String getTestReadMapping() {
		return TestContext.READ_MAPPING;
	}

	@Override
	public String getTestReadMultipleMapping() {
		return TestContext.READ_MULTIPLE_MAPPING;
	}
	
	@Override
	public String getTestDestinationMapping() {
		return TestContext.DESTINATION_MAPPING;
	}

	@Override
	public TestCompilerResult compileAndRun(String code, String... args) throws TestCompilerException {

		// compile
		List<CompiledMlm> compiledMlms;
		try {
			compiledMlms = compiler.compile(new StringReader(code));
		} catch (CompilerException e) {
			throw new TestCompilerCompiletimeException(e);
		} catch (IOException e) {
			throw new TestCompilerCompiletimeException(e);
		}

		// run and save return values
		CompiledMlm firstMlm = compiledMlms.get(0);
		TestContext context = new TestContext(compiledMlms, firstMlm.getMaintenance().getInstitution());
		TestCompilerResult result = new TestCompilerResult();
		
		ArdenValue[] returnValues;
		try {
			returnValues = firstMlm.run(context, null);
		} catch (Exception e) {
			throw new TestCompilerRuntimeException(e);
		} catch (Error e) {
			throw new TestCompilerRuntimeException(e);
		}
		if(returnValues != null) {
			for (ArdenValue returnValue : returnValues) {
				String stringValue = new NormalizedArdenValue(returnValue).toString();
				result.returnValues.add(stringValue);
			}
		}
		result.outputTexts.addAll(context.getOutputText());

		return result;
	}
	
	@Override
	public void compile(String code) throws TestCompilerCompiletimeException {
		try {
			compiler.compile(new StringReader(code));
		} catch (CompilerException e) {
			throw new TestCompilerCompiletimeException(e);
		} catch (IOException e) {
			throw new TestCompilerCompiletimeException(e);
		}
	}

}
