package org.yocto.crops.zephyr.tests;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestFailure {

	@Test(expected=AssertionError.class)
	public void testFailure() throws Exception{
		fail();
	}

}
