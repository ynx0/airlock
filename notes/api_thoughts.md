

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