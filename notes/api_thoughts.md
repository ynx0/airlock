### Jan 25
I found [this](https://stackoverflow.com/questions/2683182/how-and-where-do-you-define-your-own-exception-hierarchy-in-java) 
link which has some commentary on exception hierarchies. This was pretty much the only place I could find which actually 
talked about the situation I was in.

---
### Dec 25
One thing to watch out for is whether you need a `~` or not in a ship name property.
This is because urbit wants ships with sigs sometimes and sometimes will not. But it will treat them as two different things.
So it will fail to decode your payload if you accidentally switch it up.

To combat this, payload dataclasses need to make the appropriate calls t o `ShipName.with(out)Sig`

---
### December 23

After thinking about it, the best design I could come up with for custom exceptions is to:
- Establish a clear hierarchy of exceptions and exception types
- try never to throw generic/base class type exceptions in hte hierarchy.
- in the `throws` declaration for a method, always throw the most specific exception.
the rationale for all of this is for end user's expereince.
so that if he/she chooses to, in the `catch` clause, they can be as specific or as generic as they wish to be
  if any of the above points don't hold (i.e. flat hierarchy, invalid hierarchy that doesn't match reality, a (abstract) base class is used as a real thrown exception)
  then the most generic class will subsume any other more specific errors being thrown, which impedes ergonomics
---