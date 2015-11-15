from django.http import *
from django.shortcuts import render_to_response
from django.template import RequestContext
import socket, json, todoist

def index(request):
	return render_to_response('authenticate.html', context_instance=RequestContext(request))

def authenticate(request):
	if request.POST.has_key('username'):
		username = request.POST['username']
		password = request.POST['password']
		api = todoist.TodoistAPI()
		user = api.login(username, password)
		response_dict = {}
		response_dict.update({'server_response': user['api_token'] })
		return HttpResponse(json.dumps(response_dict))
	else:
		return render_to_response('authenticate.html', context_instance=RequestContext(request))

def createProject(apiToken, projectName):
	api = todoist.TodoistAPI(apiToken)
	project = api.projects.add(projectName.lower())
	api.commit()
	return project['id']

def getProjectId(apiToken, projectName):
	api = todoist.TodoistAPI(apiToken)
	projects = api.sync(resource_types=['projects'])
	for project in projects['Projects']:
		if project['name'].lower() == projectName.lower():
			return project['id']
	return None

def createItem(apiToken, projectId, itemName):
	api = todoist.TodoistAPI(apiToken)
	item = api.items.add(itemName.lower(), projectId)
	api.commit()
	return item['id']

def getItemId(apiToken, projectId, itemName):
	api = todoist.TodoistAPI(apiToken)
	items = api.sync(resource_types=['items'])
	for item in items['Items']:
		if item['content'].lower() == itemName.lower() and item['project_id'] == projectId and item['is_deleted'] == 0:
			return item['id']
	return None

def createProjectStub(request):
	if request.method == 'POST':
		if request.POST.has_key('projectName'):
			projectName = request.POST['projectName']
			apiToken = request.POST['apiToken']
			projectId = getProjectId(apiToken, projectName)
			response_dict = {}
			if projectId is not None:
				response_dict.update({'server_response': 'Existing Project Id: ' + str(projectId) })
			else:
				projectId = createProject(apiToken, projectName)
				response_dict.update({'server_response': 'New Project Id: ' + str(projectId) })
			return HttpResponse(json.dumps(response_dict))
	else:
		return render_to_response('createproject.html', context_instance=RequestContext(request))

def createItemStub(request):
	if request.method == 'POST':
		if request.POST.has_key('itemName'):
			projectName = request.POST['projectName']
			itemName = request.POST['itemName']
			apiToken = request.POST['apiToken']
			projectId = getProjectId(apiToken, projectName)
			itemId = getItemId(apiToken, projectId, itemName)
			response_dict = {}
			if itemId is not None:
				response_dict.update({'server_response': 'Existing Item Id: ' + str(itemId) })
			else:
				itemId = createItem(apiToken, projectId, itemName)
				response_dict.update({'server_response': 'New Item Id: ' + str(itemId) })
			return HttpResponse(json.dumps(response_dict))
	else:
		return render_to_response('createItem.html', context_instance=RequestContext(request))


def createReminderStub(request):
	if request.method == 'POST':
		if request.POST.has_key('itemName'):
			projectName = request.POST['projectName']
			itemName = request.POST['itemName']
			apiToken = request.POST['apiToken']
			locLong = request.POST['locLong']
			locLat = request.POST['locLat']
			projectId = getProjectId(apiToken, projectName)
			itemId = getItemId(apiToken, projectId, itemName)
			response_dict = {}
			if itemId is not None:
				api = todoist.TodoistAPI(apiToken)
				reminder = api.reminders.add(itemId, service='email', type='location', name='Grocery', loc_lat=locLat, loc_long=locLong, loc_trigger='on_enter', radius=100)
				api.commit()
				response_dict.update({'server_response': 'Reminder added ' + str(reminder['id']) })
			return HttpResponse(json.dumps(response_dict))
	else:
		return render_to_response('createReminder.html', context_instance=RequestContext(request))