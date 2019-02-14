#include <stdlib.h>
#include <string.h>
#include <CUnit/Basic.h>
#include "../client_server_utils.h"

void test_find_message_end () {
	char *contents = "hello\n";
	CU_ASSERT_EQUAL (5, find_message_end (contents, 0));
	CU_ASSERT_EQUAL (-1, find_message_end (contents, 6));
	CU_ASSERT_EQUAL (5, find_message_end (contents, 5));
	contents = "No\nAvatar\nis\n2\n\n\n";
	CU_ASSERT_EQUAL (2, find_message_end (contents, 0));
	CU_ASSERT_EQUAL (2, find_message_end (contents, 2));
	CU_ASSERT_EQUAL (9, find_message_end (contents, 3));
	CU_ASSERT_EQUAL (9, find_message_end (contents, 9));
	CU_ASSERT_EQUAL (12, find_message_end (contents, 11));
	CU_ASSERT_EQUAL (12, find_message_end (contents, 12));
	CU_ASSERT_EQUAL (14, find_message_end (contents, 13));
	CU_ASSERT_EQUAL (14, find_message_end (contents, 14));
	CU_ASSERT_EQUAL (15, find_message_end (contents, 15));
	CU_ASSERT_EQUAL (16, find_message_end (contents, 16));
	CU_ASSERT_EQUAL (-1, find_message_end (contents, 17));
}

void test_generate_message () {
	char contents[100] = "I like the way you move\n";
	char *new_message = generate_message (contents, strlen (contents));
	CU_ASSERT_EQUAL (0, strcmp (contents, ""));
	CU_ASSERT_EQUAL (0, strcmp (new_message, "I like the way you move\n"));
	free (new_message);
	char new_contents[40] = "I like tuh\nMake money get\n\n";
	new_message = generate_message (new_contents, strlen ("I like tuh\n"));
	CU_ASSERT_EQUAL (0, strcmp (new_message, "I like tuh\n"));
	CU_ASSERT_EQUAL (0, strcmp (new_contents, "Make money get\n\n"));
	free (new_message);
	new_message = generate_message (new_contents, strlen ("Make money get\n"));
	CU_ASSERT_EQUAL (0, strcmp (new_message, "Make money get\n"));
	CU_ASSERT_EQUAL (0, strcmp (new_contents, "\n"));
	free (new_message);
	new_message = generate_message (new_contents, 1);
	CU_ASSERT_EQUAL (0, strcmp (new_message, "\n"));
	CU_ASSERT_EQUAL (0, strcmp (new_contents, ""));
	free (new_message);
}

int main () {
	CU_pSuite pSuite = NULL;
	if (CUE_SUCCESS != CU_initialize_registry ()) {
		return CU_get_error ();
	}
	pSuite = CU_add_suite ("Testing client_server_utils", NULL, NULL);
	if (!pSuite) {
		goto exit;
	}
	if (!CU_add_test (pSuite, "find_message_end test", test_find_message_end)) {
		goto exit;
	}
	if (!CU_add_test (pSuite, "generate_message test", test_generate_message)) {
		goto exit;
	}
	CU_basic_set_mode (CU_BRM_VERBOSE);
	CU_basic_run_tests ();
exit:
	CU_cleanup_registry ();
	return CU_get_error ();
}
