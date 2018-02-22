#include "contiki.h"
#include "contiki-net.h"
#include "rest-engine.h"

#include "dev/leds.h"

#include <stdlib.h>
/*----------------------------------------------------------------------------*/
// Park Spot Resource
/*----------------------------------------------------------------------------*/

// TODO: uint8_t is too small for user probably
struct spot_data
{
	uint8_t free;
	int32_t user;
} status;

void spot_get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
void spot_post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
EVENT_RESOURCE(spot_resource, "title=\"Resource\";rt=\"Spot\"", spot_get_handler, spot_post_handler, NULL, NULL, NULL);

void spot_get_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	// Populate the buffer with the response payload
	char true_false[6];
	char usern[6];
	
	struct spot_data copy = status;
	
	if(copy.free)
		strcpy(true_false, "true");
	else
		strcpy(true_false, "false");
		
	if(copy.user < 0)
		strcpy(usern, "null");
	else
		sprintf(usern, "%d", copy.user);
	
	
	sprintf((char*) buffer, "{\"free\":%s, \"user\":%s}", true_false, usern);
	
	uint8_t length = strlen((char*)buffer);
	
	REST.set_header_content_type(response, REST.type.APPLICATION_JSON);
	REST.set_header_etag(response, (uint8_t *) &length, 1);
	REST.set_response_payload(response, buffer, length);
}


void spot_post_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	const char* value_buffer;
	int8_t free_value;
	int8_t len;
	
	len = REST.get_post_variable(request, "free", &value_buffer);
	
	printf("Received free=`%s`.\n", value_buffer);
	
	// Error, I need a value for the free argument
	if (len < 1)
	{
		printf("Invalid!\n");
		REST.set_response_status(response, REST.status.BAD_REQUEST);
		return;
	}
	
	free_value = !!atoi(value_buffer);
	
	// Cannot push two times the same value for the free argument
	if (free_value == status.free)
	{
		printf("Not modified!\n");
		REST.set_response_status(response, REST.status.NOT_MODIFIED);
		return;
	}
	
	// If the status is now free, nobody is using the spot, stop processing
	if(free_value)
	{
		printf("Set to free!\n");
		status.free = 1;
		status.user = -1;

		REST.set_response_status(response, REST.status.CHANGED);
		spot_get_handler(request, response, buffer, preferred_size, offset);
		// Notify
		REST.notify_subscribers(&spot_resource);
		printf("I did it!\n");
		return;
	}
	
	// Getting user id
	len = REST.get_post_variable(request, "user", &value_buffer);
	
	printf("Received user=`%s`.\n", value_buffer);
	
	// If the spot is busy, I need to update the user value
	if (len < 1)
	{
		printf("Invalid!\n");
		REST.set_response_status(response, REST.status.BAD_REQUEST);
		return;
	}
	
	status.free = 0;
	status.user = atoi(value_buffer);
	
	printf("Assigned to user %d!\n", status.user);
	
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
	
	status.free = 1;
	status.user = -1;

	rest_activate_resource(&spot_resource, "park_spot");
	
	while(1) {
		PROCESS_WAIT_EVENT();
	}
	
	PROCESS_END();
}
