Multi-Messages
==============

Multi Messages Play Framework2 Plugin


Usage
=====

Create as many folders under your conf project folder as locales your project needs to support using the locale codes.

Add as many properties files as you want in the locale folders and keep a record of the filenames in a file called messagelisting.properties in your conf folder. You can change the message listing file name if you specify it on a configuration property called i18n.messagelisting

In your project make sure to use play.api.i18n.MMessages for the translations. You can migrate your project just using the import statement:
import play.api.i18n.{MMessages => Messages}



Be aware that the default Play component templating (e.g. FieldElements when using FieldConstructors) use hardcoded Messages library, so you will have to create workarounds or implicit conversions
