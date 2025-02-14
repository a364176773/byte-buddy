package net.bytebuddy.agent.builder;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.JavaModule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;

import java.util.concurrent.ExecutorService;

import static net.bytebuddy.test.utility.FieldByFieldComparison.hasPrototype;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AgentBuilderDescriptionStrategyTest {

    @Rule
    public MethodRule mockitoRule = MockitoJUnit.rule().silent();

    @Mock
    private AgentBuilder.LocationStrategy locationStrategy;

    @Mock
    private TypePool typePool;

    @Mock
    private TypeDescription typeDescription;

    @Test
    public void testDescriptionHybridWithLoaded() throws Exception {
        ClassFileLocator classFileLocator = ClassFileLocator.ForClassLoader.of(Object.class.getClassLoader());
        when(typePool.describe(Object.class.getName())).thenReturn(new TypePool.Resolution.Simple(typeDescription));
        when(locationStrategy.classFileLocator(Object.class.getClassLoader(), JavaModule.ofType(Object.class))).thenReturn(classFileLocator);
        TypeDescription typeDescription = AgentBuilder.DescriptionStrategy.Default.HYBRID.apply(Object.class.getName(),
                Object.class,
                typePool,
                mock(AgentBuilder.CircularityLock.class),
                Object.class.getClassLoader(),
                JavaModule.ofType(Object.class));
        assertThat(typeDescription, is(TypeDescription.OBJECT));
        assertThat(typeDescription, instanceOf(TypeDescription.ForLoadedType.class));
    }

    @Test
    public void testDescriptionHybridWithoutLoaded() throws Exception {
        when(typePool.describe(Object.class.getName())).thenReturn(new TypePool.Resolution.Simple(typeDescription));
        TypeDescription typeDescription = AgentBuilder.DescriptionStrategy.Default.HYBRID.apply(Object.class.getName(),
                null,
                typePool,
                mock(AgentBuilder.CircularityLock.class),
                Object.class.getClassLoader(),
                JavaModule.ofType(Object.class));
        assertThat(typeDescription, is(this.typeDescription));
    }

    @Test
    public void testDescriptionPoolOnly() throws Exception {
        when(typePool.describe(Object.class.getName())).thenReturn(new TypePool.Resolution.Simple(typeDescription));
        assertThat(AgentBuilder.DescriptionStrategy.Default.POOL_ONLY.apply(Object.class.getName(),
                Object.class,
                typePool,
                mock(AgentBuilder.CircularityLock.class),
                Object.class.getClassLoader(),
                JavaModule.ofType(Object.class)), is(typeDescription));
    }

    @Test
    public void testSuperTypeLoading() throws Exception {
        assertThat(AgentBuilder.DescriptionStrategy.Default.HYBRID.withSuperTypeLoading(),
                hasPrototype((AgentBuilder.DescriptionStrategy) new AgentBuilder.DescriptionStrategy.SuperTypeLoading(AgentBuilder.DescriptionStrategy.Default.HYBRID)));
        assertThat(AgentBuilder.DescriptionStrategy.Default.POOL_FIRST.withSuperTypeLoading(),
                hasPrototype((AgentBuilder.DescriptionStrategy) new AgentBuilder.DescriptionStrategy.SuperTypeLoading(AgentBuilder.DescriptionStrategy.Default.POOL_FIRST)));
        assertThat(AgentBuilder.DescriptionStrategy.Default.POOL_ONLY.withSuperTypeLoading(),
                hasPrototype((AgentBuilder.DescriptionStrategy) new AgentBuilder.DescriptionStrategy.SuperTypeLoading(AgentBuilder.DescriptionStrategy.Default.POOL_ONLY)));
    }

    @Test
    public void testAsynchronousSuperTypeLoading() throws Exception {
        ExecutorService executorService = mock(ExecutorService.class);
        assertThat(AgentBuilder.DescriptionStrategy.Default.HYBRID.withSuperTypeLoading(executorService),
                hasPrototype((AgentBuilder.DescriptionStrategy) new AgentBuilder.DescriptionStrategy.SuperTypeLoading.Asynchronous(AgentBuilder.DescriptionStrategy.Default.HYBRID, executorService)));
        assertThat(AgentBuilder.DescriptionStrategy.Default.POOL_FIRST.withSuperTypeLoading(executorService),
                hasPrototype((AgentBuilder.DescriptionStrategy) new AgentBuilder.DescriptionStrategy.SuperTypeLoading.Asynchronous(AgentBuilder.DescriptionStrategy.Default.POOL_FIRST, executorService)));
        assertThat(AgentBuilder.DescriptionStrategy.Default.POOL_ONLY.withSuperTypeLoading(executorService),
                hasPrototype((AgentBuilder.DescriptionStrategy) new AgentBuilder.DescriptionStrategy.SuperTypeLoading.Asynchronous(AgentBuilder.DescriptionStrategy.Default.POOL_ONLY, executorService)));
    }
}
