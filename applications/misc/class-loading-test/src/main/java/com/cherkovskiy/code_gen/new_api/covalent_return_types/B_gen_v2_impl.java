package com.cherkovskiy.code_gen.new_api.covalent_return_types;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

public class B_gen_v2_impl implements B_gen_v2 {
    @Override
    public String simpleArg(String str, A_gen_v2 a, Collection<Object> obj) {
        //todo: original logic
        System.out.println(a.newMethod()); // убедиться что он поддерживается
        return "____";
    }


    //TODO: генерируем
    @Override
    public String simpleArg(String str, A a, Collection<Object> obj) {
        //create simple proxy
        //simpleArg(___generate_a_gen_v2_proxy(a));//todo: use this option
        return simpleArg(str, new A_proxy_gen_v2(a), obj);
    }

    @Override
    public A_gen_v2 covalent() {
        //todo: original logic
        return new A_gen_v2() {
            @Nonnull
            @Override
            public String newMethod() {
                return "newMethod";
            }

            @Override
            public String newMethodNullable() {
                return "newMethodNullable";
            }

            @Override
            public Collection<String> method(String arg1, List<LocalDateTime> times, int num) {
                return Collections.singletonList("method");
            }

            @Override
            public String newMethodDefault() {
                return "newMethodDefault";
            }
        };
    }

    //generate
    //  a lot of proxies for all interfaces (((
    @Override
    public void argListMethod(List<A> listOfA) {
        argListMethod_gen_v2(
                new List<A_gen_v2>() {
                    @Override
                    public int size() {
                        return listOfA.size();
                    }

                    @Override
                    public boolean isEmpty() {
                        return listOfA.isEmpty();
                    }

                    @Override
                    public boolean contains(Object o) {
                        return listOfA.contains(o);
                    }

                    @Override
                    public Iterator<A_gen_v2> iterator() {
                        Iterator<A> iterator = listOfA.iterator(); //TODO: нужно запоминать!
                        return new Iterator<A_gen_v2>() {
                            @Override
                            public boolean hasNext() {
                                return iterator.hasNext();
                            }

                            @Override
                            public A_gen_v2 next() {
                                A a = iterator.next(); //TODO: нужно запоминать!
                                return ___generate_a_gen_v2_proxy(a);
                            }
                        };
                    }

                    @Override
                    public Object[] toArray() {
                        return listOfA.toArray();
                    }

                    @Override
                    public <T> T[] toArray(T[] ts) {
                        return listOfA.toArray(ts);
                    }

                    @Override
                    public boolean add(A_gen_v2 a_gen_v2) {
                        return listOfA.add(a_gen_v2);
                    }

                    @Override
                    public boolean remove(Object o) {
                        return listOfA.remove(o);
                    }

                    @Override
                    public boolean containsAll(Collection<?> collection) {
                        return listOfA.contains(collection);
                    }

                    @Override
                    public boolean addAll(Collection<? extends A_gen_v2> collection) {
                        return listOfA.addAll(collection);
                    }

                    @Override
                    public boolean addAll(int i, Collection<? extends A_gen_v2> collection) {
                        return false;
                    }

                    @Override
                    public boolean removeAll(Collection<?> collection) {
                        return false;
                    }

                    @Override
                    public boolean retainAll(Collection<?> collection) {
                        return false;
                    }

                    @Override
                    public void clear() {

                    }

                    @Override
                    public A_gen_v2 get(int i) {
                        return null;
                    }

                    @Override
                    public A_gen_v2 set(int i, A_gen_v2 a_gen_v2) {
                        return null;
                    }

                    @Override
                    public void add(int i, A_gen_v2 a_gen_v2) {

                    }

                    @Override
                    public A_gen_v2 remove(int i) {
                        return ___generate_a_gen_v2_proxy(listOfA.remove(i));
                    }

                    @Override
                    public int indexOf(Object o) {
                        return 0;
                    }

                    @Override
                    public int lastIndexOf(Object o) {
                        return 0;
                    }

                    @Override
                    public ListIterator<A_gen_v2> listIterator() {
                        ListIterator<A> orig = listOfA.listIterator();
                        return new ListIterator<A_gen_v2>() {
                            @Override
                            public boolean hasNext() {
                                return false;
                            }

                            @Override
                            public A_gen_v2 next() {
                                return ___generate_a_gen_v2_proxy(orig.next());
                            }

                            @Override
                            public boolean hasPrevious() {
                                return false;
                            }

                            @Override
                            public A_gen_v2 previous() {
                                return ___generate_a_gen_v2_proxy(orig.previous());
                            }

                            @Override
                            public int nextIndex() {
                                return 0;
                            }

                            @Override
                            public int previousIndex() {
                                return 0;
                            }

                            @Override
                            public void remove() {

                            }

                            @Override
                            public void set(A_gen_v2 a_gen_v2) {
                                orig.set(a_gen_v2);
                            }

                            @Override
                            public void add(A_gen_v2 a_gen_v2) {

                            }
                        };
                    }

                    @Override
                    public ListIterator<A_gen_v2> listIterator(int i) {
                        return null;
                    }

                    @Override
                    public List<A_gen_v2> subList(int i, int i1) {
                        return null;
                    }
                }
        );
    }

    private A_gen_v2 ___generate_a_gen_v2_proxy(A a) {
        if (a instanceof A_gen_v2) {
            return (A_gen_v2) a;
        }

        return new A_proxy_gen_v2(a);
    }

    //generate
    @Override
    public List<A> retListMethod() {
        List<A_gen_v2> orig = retListMethod_gen_v2();
        return new List<A>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            public Iterator<A> iterator() {
                Iterator<A_gen_v2> origItr = orig.iterator();
                return new Iterator<A>() {
                    @Override
                    public boolean hasNext() {
                        return origItr.hasNext();
                    }

                    @Override
                    public A next() {
                        return origItr.next();
                    }

                    @Override
                    public void remove() {

                    }

                    @Override
                    public void forEachRemaining(Consumer<? super A> consumer) {

                    }

                    @Override
                    public int hashCode() {
                        return super.hashCode();
                    }

                    @Override
                    public boolean equals(Object o) {
                        return super.equals(o);
                    }

                    @Override
                    public String toString() {
                        return super.toString();
                    }
                };
            }

            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @Override
            public <T> T[] toArray(T[] ts) {
                return null;
            }

            @Override
            public boolean add(A a) {
                return orig.add(___generate_a_gen_v2_proxy(a));
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> collection) {
                return false;
            }

            @Override
            public boolean addAll(Collection<? extends A> collection) {
                return orig.addAll(new Collection<A_gen_v2>() {
                    @Override
                    public int size() {
                        return 0;
                    }

                    @Override
                    public boolean isEmpty() {
                        return false;
                    }

                    @Override
                    public boolean contains(Object o) {
                        return false;
                    }

                    @Override
                    public Iterator<A_gen_v2> iterator() {
                        return null;
                    }

                    @Override
                    public Object[] toArray() {
                        return new Object[0];
                    }

                    @Override
                    public <T> T[] toArray(T[] ts) {
                        return null;
                    }

                    @Override
                    public boolean add(A_gen_v2 a_gen_v2) {
                        throw new UnsupportedOperationException("мол добавлять к коллекцию предназначенную для чтения НЕЛЬЗЯ");
                    }

                    @Override
                    public boolean remove(Object o) {
                        return false;
                    }

                    @Override
                    public boolean containsAll(Collection<?> collection) {
                        return false;
                    }

                    @Override
                    public boolean addAll(Collection<? extends A_gen_v2> collection) {
                        return false;
                    }

                    @Override
                    public boolean removeAll(Collection<?> collection) {
                        return false;
                    }

                    @Override
                    public boolean retainAll(Collection<?> collection) {
                        return false;
                    }

                    @Override
                    public void clear() {

                    }
                });
            }

            @Override
            public boolean addAll(int i, Collection<? extends A> collection) {
                return false;
            }

            @Override
            public boolean removeAll(Collection<?> collection) {
                return false;
            }

            @Override
            public boolean retainAll(Collection<?> collection) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public A get(int i) {
                return null;
            }

            @Override
            public A set(int i, A a) {
                return null;
            }

            @Override
            public void add(int i, A a) {

            }

            @Override
            public A remove(int i) {
                return null;
            }

            @Override
            public int indexOf(Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(Object o) {
                return 0;
            }

            @Override
            public ListIterator<A> listIterator() {
                return null;
            }

            @Override
            public ListIterator<A> listIterator(int i) {
                return null;
            }

            @Override
            public List<A> subList(int i, int i1) {
                return null;
            }
        };
    }

    //change method name
    @Override
    public void argListMethod_gen_v2(List<A_gen_v2> listOfA) {
        //todo: original logic
    }

    //change method name
    @Override
    public List<A_gen_v2> retListMethod_gen_v2() {
        //todo: original logic
        return null;
    }


    //generate
    @Override
    public void extendedReadGenerics(Collection<? extends A> collection) {
        extendedGenerics_gen_v2(new Collection<A_gen_v2>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            public Iterator<A_gen_v2> iterator() {
                return null;
            }

            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @Override
            public <T> T[] toArray(T[] ts) {
                return null;
            }

            @Override
            public boolean add(A_gen_v2 a_gen_v2) {
                throw new UnsupportedOperationException("мол добавлять к коллекцию предназначенную для чтения НЕЛЬЗЯ");
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> collection) {
                return false;
            }

            @Override
            public boolean addAll(Collection<? extends A_gen_v2> collection) {
                return false;
            }

            @Override
            public boolean removeAll(Collection<?> collection) {
                return false;
            }

            @Override
            public boolean retainAll(Collection<?> collection) {
                return false;
            }

            @Override
            public void clear() {

            }
        });
    }


    //change method name
    @Override
    public void extendedGenerics_gen_v2(Collection<? extends A_gen_v2> collection) {

    }

    @Override
    public void extendedWriteGenerics(Collection<? super A> collection) {
        extendedWriteGenerics_gen_v2(new Collection<A_gen_v2>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            public Iterator<A_gen_v2> iterator() {
                Iterator<? super A> itr = collection.iterator();
                return new Iterator<A_gen_v2>() {
                    @Override
                    public boolean hasNext() {
                        return false;
                    }

                    @Override
                    public A_gen_v2 next() {
                        //return itr.next(); //read is prohibited
                        throw new UnsupportedOperationException("Мол коллекция только на запись");
                    }
                };
            }

            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @Override
            public <T> T[] toArray(T[] ts) {
                return null;
            }

            @Override
            public boolean add(A_gen_v2 a_gen_v2) {
                return collection.add(a_gen_v2);//add is ok
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> collection) {
                return false;
            }

            @Override
            public boolean addAll(Collection<? extends A_gen_v2> collection) {
                return false;
            }

            @Override
            public boolean removeAll(Collection<?> collection) {
                return false;
            }

            @Override
            public boolean retainAll(Collection<?> collection) {
                return false;
            }

            @Override
            public void clear() {

            }
        });
    }


    //change method name
    @Override
    public void extendedWriteGenerics_gen_v2(Collection<? super A_gen_v2> collection) {

    }


    //todo
    //--------------------------------------------------------------------------------------------
    @Override
    public void unknownGenericTypes(UnknownType<A> aUnknownType) {

    }

    @Override
    public UnknownType<A> retUnknownGenericTypes() {
        return null;
    }

    //proxy
    @Override
    public void compositeGenericTypes(Map<UnknownType<A>, A> unknownTypeAMap) {
        compositeGenericTypes_gen_v2(new Map<UnknownType<A_gen_v2>, A_gen_v2>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean containsKey(Object o) {
                return false;
            }

            @Override
            public boolean containsValue(Object o) {
                return false;
            }

            @Override
            public A_gen_v2 get(Object o) {
                return null;
            }

            @Override
            public A_gen_v2 put(UnknownType<A_gen_v2> a_gen_v2UnknownType, A_gen_v2 a_gen_v2) {
                return ___generate_a_gen_v2_proxy(unknownTypeAMap.put(
                        new UnknownType<A>() {
                            @Override
                            public A get() {
                                return a_gen_v2UnknownType.get();
                            }
                        },
                        ___generate_a_gen_v2_proxy(a_gen_v2)
                ));
            }

            @Override
            public A_gen_v2 remove(Object o) {
                return null;
            }

            @Override
            public void putAll(Map<? extends UnknownType<A_gen_v2>, ? extends A_gen_v2> mapOrig) {
                unknownTypeAMap.putAll(new Map<UnknownType<A>, A>() {
                    @Override
                    public int size() {
                        return 0;
                    }

                    @Override
                    public boolean isEmpty() {
                        return false;
                    }

                    @Override
                    public boolean containsKey(Object o) {
                        return false;
                    }

                    @Override
                    public boolean containsValue(Object o) {
                        return false;
                    }

                    @Override
                    public A get(Object o) {
                        return null;
                    }

                    @Override
                    public A put(UnknownType<A> aUnknownType, A a) {
                        return null;
                    }

                    @Override
                    public A remove(Object o) {
                        return null;
                    }

                    @Override
                    public void putAll(Map<? extends UnknownType<A>, ? extends A> map) {
                        throw new UnsupportedOperationException("мол добавлять к коллекцию предназначенную для чтения НЕЛЬЗЯ");
                    }

                    @Override
                    public void clear() {

                    }

                    @Override
                    public Set<UnknownType<A>> keySet() {
                        return null;
                    }

                    @Override
                    public Collection<A> values() {
                        return null;
                    }

                    @Override
                    public Set<Entry<UnknownType<A>, A>> entrySet() {
                        return null;
                    }
                });
            }

            @Override
            public void clear() {

            }

            @Override
            public Set<UnknownType<A_gen_v2>> keySet() {
                return null;
            }

            @Override
            public Collection<A_gen_v2> values() {
                return null;
            }

            @Override
            public Set<Entry<UnknownType<A_gen_v2>, A_gen_v2>> entrySet() {
                return null;
            }
        });
    }

    //change method name
    @Override
    public void compositeGenericTypes_gen_v2(Map<UnknownType<A_gen_v2>, A_gen_v2> unknownTypeAMap) {

    }

}
