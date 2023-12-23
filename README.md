It is common practice in software development to use multiple programming languages within a single project, known as multilingual development. 
While leveraging multiple programming languages in the development of complex systems offers substantial advantages like legacy integration and 
performance optimization, integrating code from various languages can introduce code smells that adversely affect the
overall readability, maintainability, and performance of these
systems. These code smells are called multi-language code
smells. Unused Native Method declaration and Unused Native
Method Implementation are two of the multi-language code
smells that are related to unused code. These code smells unnecessarily increase the volume of the codebase, which leads
to difficulties in understanding and maintaining the code.
Since these smells negatively impact the non-functional requirement of a software system, they must be detected and
refactored. The detection approach proposed in the literature had many limitations, because of which the results
presented in the literature about the prevalence of these two
smells were incorrect. This paper addresses the identified
limitations and assesses the existence of these two smells
in open-source projects. Our results show that 9.34% of JNI
files are affected by Unused Native Method Declaration code
smell, and 46.18% of JNI files are affected by Unused Native
Method Implementation code smell, which confirms the existence of these two code smells in open source multi-language
systems.
