from django.conf.urls import patterns, include, url
from simplist import views

urlpatterns = patterns('',
    url(r'^$', views.index, name='index'),
    url(r'^authenticate$', views.authenticate, name='authenticate'),
    url(r'^createProject$', views.createProjectStub, name='createProjectStub'),
    url(r'^createItem$', views.createItemStub, name='createItemStub'),
    url(r'^createReminder$', views.createReminderStub, name='createReminderStub'),
)
