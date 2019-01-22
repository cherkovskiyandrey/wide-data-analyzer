package com.cherkovskiy.application_context.configuration.resources;

import com.cherkovskiy.application_context.api.configuration.resources.MutableResource;
import com.cherkovskiy.application_context.api.configuration.resources.NamedResource;
import com.cherkovskiy.application_context.configuration.sources.AbstractPropertySource;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MutableResourcesImpl<T extends NamedResource> implements MutableResource<T> {
    private final List<ResourceHolder<T>> resources = new CopyOnWriteArrayList<>();

    @Override
    public boolean contains(@Nonnull String name) {
        return resources.contains(ResourceHolder.<T>ofStub(name));
    }

    @Override
    public T get(@Nonnull String name) {
        int index = resources.indexOf(ResourceHolder.<T>ofStub(name));
        return (index != -1 ? resources.get(index).getResource() : null);
    }

    @Override
    public Iterator<T> iterator() {
        final Iterator<ResourceHolder<T>> iterator = resources.iterator();
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next().getResource();
            }
        };
    }

    @Override
    public void addFirst(@Nonnull T resource) {
        removeIfPresent(resource);
        resources.add(0, ResourceHolder.of(resource));
    }

    @Override
    public void addLast(@Nonnull T resource) {
        removeIfPresent(resource);
        resources.add(ResourceHolder.of(resource));
    }

    @Override
    public void addBefore(@Nonnull String relativePropertySourceName, @Nonnull T resource) {
        assertLegalRelativeAddition(relativePropertySourceName, resource);
        removeIfPresent(resource);
        int index = assertPresentAndGetIndex(relativePropertySourceName);
        addAtIndex(index, resource);
    }

    @Override
    public void addAfter(@Nonnull String relativePropertySourceName, @Nonnull T resource) {
        assertLegalRelativeAddition(relativePropertySourceName, resource);
        removeIfPresent(resource);
        int index = assertPresentAndGetIndex(relativePropertySourceName);
        addAtIndex(index + 1, resource);
    }

    @Override
    public T remove(@Nonnull String name) {
        int index = resources.indexOf(ResourceHolder.ofStub(name));
        return (index != -1 ? resources.remove(index).getResource() : null);
    }

    @Override
    public void replace(@Nonnull String name, @Nonnull T resource) {
        int index = assertPresentAndGetIndex(name);
        resources.set(index, ResourceHolder.of(resource));
    }

    @Override
    public int size() {
        return resources.size();
    }

    /**
     * Remove the given property source if it is present.
     */
    private void removeIfPresent(T resource) {
        resources.remove(ResourceHolder.of(resource));
    }

    /**
     * Ensure that the given property source is not being added relative to itself.
     */
    private void assertLegalRelativeAddition(String relativePropertySourceName, T resource) {
        String newPropertySourceName = resource.getName();
        if (relativePropertySourceName.equals(newPropertySourceName)) {
            throw new IllegalArgumentException(
                    String.format("AbstractPropertySource named [%s] cannot be added relative to itself", newPropertySourceName));
        }
    }

    /**
     * Assert that the named property source is present and return its index.
     *
     * @param name the {@linkplain AbstractPropertySource#getName() name of the property source}
     *             to find
     * @throws IllegalArgumentException if the named property source is not present
     */
    private int assertPresentAndGetIndex(String name) {
        int index = resources.indexOf(ResourceHolder.<T>ofStub(name));
        if (index == -1) {
            throw new IllegalArgumentException(String.format("AbstractPropertySource named [%s] does not exist", name));
        }
        return index;
    }

    /**
     * Add the given property source at a particular index in the list.
     */
    private void addAtIndex(int index, T resource) {
        removeIfPresent(resource);
        resources.add(index, ResourceHolder.of(resource));
    }

    @Override
    public String toString() {
        return "MutableResourcesImpl{}";
    }
}
