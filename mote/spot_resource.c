#include "contiki.h"
#include "contiki-net.h"
#include "rest-engine.h"

#include "dev/leds.h"

#include <stdlib.h>
/*----------------------------------------------------------------------------*/
// Park Spot Resource
/*----------------------------------------------------------------------------*/

struct spot_data
{
	uint8_t free;
	char user[64];
	char credit[17];
} status;

#define STATUS_FREE		1
#define STATUS_OCCUPIED 0
#define STATUS_WAITING	2

void spot_get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void spot_post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
EVENT_RESOURCE(spot_resource, "title=\"Resource\";rt=\"Spot\"", spot_get_handler, spot_post_handler, NULL, NULL, NULL);

void spot_get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	// Populate the buffer with the response payload
	char true_false[6];
	char usern[68];
	char credit[17];
	
	struct spot_data copy = status;
	
	if(copy.free != STATUS_OCCUPIED)
		strcpy(true_false, "true");
	else
		strcpy(true_false, "false");
	
	if(strncmp(copy.user, "null", strlen("null")) == 0)
	{
		strcpy(usern, "null");
		strcpy(credit, "null");
	}
	else
	{
		sprintf(usern, "\"%s\"", copy.user);
		sprintf(credit, "\"%s\"", copy.credit);
	}
	
	
	sprintf((char*) buffer, "{\"free\":%s, \"user\":%s, \"credit\":%s}", true_false, usern, credit);
	
	uint8_t length = strlen((char*)buffer);
	
	REST.set_header_content_type(response, REST.type.APPLICATION_JSON);
	REST.set_header_etag(response, (uint8_t *) &length, 1);
	REST.set_response_payload(response, buffer, length);
}


void spot_post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	const char* value_buffer;
	const char* credit_buffer;
	int8_t free_value;
	int8_t len, credit_len;
	
	len = REST.get_post_variable(request, "free", &value_buffer);
	
	printf("Received free=`%s`.\n", value_buffer);
	
	// Error, I need a value for the free argument
	if (len < 1)
	{
		printf("Invalid!\n");
		REST.set_response_status(response, REST.status.BAD_REQUEST);
		return;
	}
	
	free_value = atoi(value_buffer);

	if (free_value > 2)
	{
		printf("Invalid value!\n");
		REST.set_response_status(response, REST.status.NOT_MODIFIED);
		return;
	}
	
	// Cannot push two times the same value for the free argument
	if (free_value == status.free)
	{
		printf("Not modified!\n");
		REST.set_response_status(response, REST.status.NOT_MODIFIED);
		return;
	}
	
	// If the status is now free, nobody is using the spot, stop processing
	if(free_value == STATUS_FREE)
	{
		printf("Set to free!\n");
		status.free = STATUS_FREE;
		strcpy(status.user, "null");

		REST.set_response_status(response, REST.status.CHANGED);
		spot_get_handler(request, response, buffer, preferred_size, offset);
		// Notify
		REST.notify_subscribers(&spot_resource);
		printf("I did it!\n");
		return;
	}
	
	if(free_value == STATUS_OCCUPIED)
	{
		if (status.free != STATUS_WAITING)
		{
			printf("Not modified!\n");
			REST.set_response_status(response, REST.status.NOT_MODIFIED);
			return;
		}

		status.free = STATUS_OCCUPIED;
		printf("New user is %s.\n", status.user);
		REST.set_response_status(response, REST.status.CHANGED);
		REST.notify_subscribers(&spot_resource);
		printf("I did it!\n");
		return;
	}

	if(status.free != STATUS_FREE)
	{
		printf("Not modified!\n");
		REST.set_response_status(response, REST.status.NOT_MODIFIED);
		return;
	}

	// Getting user id and credit
	len = REST.get_post_variable(request, "user", &value_buffer);
	credit_len = REST.get_post_variable(request, "credit", &credit_buffer);

	if(value_buffer[len-1] == '\n')
		--len;
	if(credit_buffer[credit_len-1] == '\n')
		--credit_len;
	
	printf("Received user=`%s`.\n", value_buffer);
	printf("Received credit=`%s`.\n", credit_buffer);
	
	if (len < 1 || credit_len < 1)
	{
		printf("Invalid!\n");
		REST.set_response_status(response, REST.status.BAD_REQUEST);
		return;
	}
	
	// If the spot is busy, I need to update the user value
	status.free = STATUS_WAITING;
	strncpy(status.user, value_buffer, len);
	status.user[len] = '\0';

	strncpy(status.credit, credit_buffer, credit_len);
	status.credit[credit_len] = '\0';
	
	printf("Spot is waiting for response for user %s and credit card %s!\n", status.user, status.credit);
	
	REST.set_response_status(response, REST.status.CHANGED);
	spot_get_handler(request, response, buffer, preferred_size, offset);
	// Notify
	REST.notify_subscribers(&spot_resource);
	printf("I did it!\n");
}


// Process definition
PROCESS(server, "CoAP Server");
AUTOSTART_PROCESSES(&server);

PROCESS_THREAD(server, ev, data){
	PROCESS_BEGIN();
	rest_init_engine();
	
	status.free = STATUS_FREE;
	strcpy(status.user, "null");

	rest_activate_resource(&spot_resource, "park_spot");
	
	while(1) {
		PROCESS_WAIT_EVENT();
	}
	
	PROCESS_END();
}
