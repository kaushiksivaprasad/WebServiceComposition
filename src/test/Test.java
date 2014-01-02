package test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ca.concordia.pga.models.Concept;

public class Test {
public static void main(String[] args) {
	Set<Concept> set = new HashSet<Concept>();
	Concept c[] = new Concept[4];
	for(int i = 0; i < c.length; i++)
	{
		c[i] = new Concept("Kaushik"+i);
		set.add(c[i]);
	}
	int i = 0;
	Iterator<Concept> iter = set.iterator();
	while(iter.hasNext())
	{
		Concept x = iter.next();
		System.out.println(x);
		if(i == 1)
		{
			System.out.println("Before removing..");
			iter.remove();
		}
		i++;
	}
	System.out.println(set);
}
}
