package wdl;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class ReflectionUtilsTest {

	@Test
	public void testPrimitives() {
		@SuppressWarnings("unused")
		class Test {
			private int primitive;
			private Integer boxed;
		}

		assertThat(ReflectionUtils.findField(Test.class, int.class).getName(), is("primitive"));
		assertThat(ReflectionUtils.findField(Test.class, Integer.class).getName(), is("boxed"));
	}

}
