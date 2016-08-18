/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.eifadapter;

import java.util.*;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertTrue;
import static org.opennms.features.eifadapter.EifParser.parseEifSlots;
import static org.opennms.features.eifadapter.EifParser.translateEifToOpenNMS;
import org.opennms.netmgt.xml.event.Event;

public class TestEifTranslator {

    @Test
    public void EifTranslatorTest() {
        String incomingEif = "<START>>......................LL.....EIF_EVENT_TYPE_A;cms_hostname='htems_host';"
                +"cms_port='3661';integration_type='N';master_reset_flag='';appl_label='';"
                +"situation_name='DummyMonitoringSituation';situation_type='S';situation_origin='dummyHost:08';"
                +"situation_time='07/22/2016 14:05:36.000';situation_status='Y';situation_thrunode='REMOTE_teps_host';"
                +"situation_displayitem='';source='EIF_TEST';sub_source='dummyHost:08';hostname='dummyHost';"
                +"origin='10.0.0.7';adapter_host='dummyHost';date='07/22/2016';severity='WARNING';"
                +"msg='My Dummy Event Message';situation_eventdata='~';END";
        Event e = translateEifToOpenNMS(new StringBuilder(incomingEif)).get(0);
        assertEquals("uei.opennms.org/vendor/IBM/EIF/EIF_EVENT_TYPE_A",e.getUei());
        assertEquals("DummyMonitoringSituation",e.getParm("situation_name").getValue().getContent());
    }

    @Test
    public void canParseEifSlotsTest() {
        String eifBody = "integration_type='N';master_reset_flag='';appl_label='';"
                +"situation_name='DummyMonitoringSituation';situation_type='S';"
                +"situation_origin='dummyHost:08';situation_time='07/22/2016 14:05:36.000';situation_status='Y';"
                +"situation_thrunode='REMOTE_teps_host';situation_displayitem='';source='EIF_TEST';"
                +"sub_source='dummyHost:08';hostname='dummyHost';origin='10.0.0.7';adapter_host='dummyHost';"
                +"date='07/22/2016';severity='WARNING';msg='My Dummy Event Message';situation_eventdata='~';";
        Map<String, String> eifSlotMap = parseEifSlots(eifBody);
        assertEquals("EIF_TEST",eifSlotMap.get("source"));
        assertEquals("dummyHost",eifSlotMap.get("adapter_host"));
        assertEquals("REMOTE_teps_host",eifSlotMap.get("situation_thrunode"));
    }

    @Test
    public void canParseEifSlotsWithEmbeddedSemicolonsTest() {
        String eifBody = "cms_hostname='hubtems01';cms_port='3661';"
                +"integration_type='U';master_reset_flag='';appl_label='';situation_name='Situation 01';"
                +"situation_type='S';situation_origin='';situation_time='07/28/2016 12:19:11.000';situation_status='P';"
                +"situation_thrunode='REMOTE_teps_host';situation_fullname='Situation 01';situation_displayitem='';"
                +"source='EIF_TEST';sub_source='';hostname='';origin='';adapter_host='dummyHost';date='07/28/2016';"
                +"severity='CRITICAL';IncidentSupportTeam='Server Support Testing';"
                +"semicolon_test='this is a test; of semicolons in; slot values';"
                +"onClose_msg='Event closed. OpenNMS EIF Testing.';onClose_severity='WARNING';send_delay='6';"
                +"msg='This is a test of EIF for OpenNMS';situation_eventdata='~';END";
        Map<String, String> eifSlotMap = parseEifSlots(eifBody);
        assertEquals("EIF_TEST",eifSlotMap.get("source"));
        assertEquals("dummyHost",eifSlotMap.get("adapter_host"));
        assertEquals("REMOTE_teps_host",eifSlotMap.get("situation_thrunode"));
    }

    @Test
    public void EifTranslatorMultipleEventsTest() {
        String incomingEif_1 = ".<START>>.......................0.....EIF_TEST_EVENT_TYPE_A;cms_hostname='hubtems01';"
                +"cms_port='3661';integration_type='N';master_reset_flag='';appl_label='';"
                +"situation_name='DummyMonitoringSituation';situation_type='S';situation_origin='managedsystem01:08';"
                +"situation_time='07/22/2016 14:05:36.000';situation_status='Y';situation_thrunode='REMOTE_rtems01';"
                +"situation_displayitem='';source='EIF';sub_source='managedsystem01:08';hostname='managedsystem01';"
                +"origin='10.0.0.1';adapter_host='managedsystem01';date='07/22/2016';severity='WARNING';"
                +"msg='EIF Test Message 1';situation_eventdata='~';END";
        String incomingEif_2 = ".<START>>.............................EIF_TEST_EVENT_TYPE_B;cms_hostname='hubtems01';"
                +"cms_port='3661';integration_type='U';master_reset_flag='';appl_label='';"
                +"situation_name='DummyMonitoringSituation';situation_type='S';situation_origin='managedsystem02:LZ';"
                +"situation_time='07/22/2016 14:07:52.000';situation_status='Y';situation_thrunode='HUB_hubtems01';"
                +"situation_displayitem='';source='EIF';sub_source='managedsystem02:LZ';hostname='managedsystem02';"
                +"origin='10.0.0.2';adapter_host='managedsystem02';date='07/22/2016';severity='HARMLESS';"
                +"msg='EIF_Heartbeat';situation_eventdata='Day_Of_Month=22;Day_Of_Week=06;Hours=15;Minutes=50;"
                +"Month_Of_Year=07;System_Name=managedsystem02:LZ;Seconds=25;Timestamp=1160722155025000~';END";
        String incomingEif_3 = ".<START>>.............................EIF_TEST_EVENT_TYPE_A;cms_hostname='hubtems01';"
                +"cms_port='3661';integration_type='U';master_reset_flag='';appl_label='';"
                +"situation_name='DummyMonitoringSituation';situation_type='S';situation_origin='managedsystem03:LZ';"
                +"situation_time='07/22/2016 14:08:07.000';situation_status='Y';situation_thrunode='REMOTE_hubtems02';"
                +"situation_displayitem='';source='EIF';sub_source='managedsystem03:LZ';hostname='managedsystem03';"
                +"origin='10.0.0.3';adapter_host='managedsystem03';date='07/22/2016';severity='CRITICAL';"
                +"SupportGroup='Server Support';custom_slot0='Server Support';priority='2';"
                +"msg='EIF Test Message 3';situation_eventdata='test_command;"
                +"System_Name=managedsystem03:LZ~test_command;System_Name=managedsystem03:LZ~';END";
        String multipleEif = new StringBuilder(incomingEif_1).append("\n").append(incomingEif_2).append("\n").
                append(incomingEif_3).append("\n").toString();

        List<Event> events = translateEifToOpenNMS(new StringBuilder(multipleEif));
        assertTrue("Event list must not be null", events != null);
        for (Event event : events) {
            System.out.println("Evaluating UEI regex on "+event.getUei());
            assertTrue("UEI must match regex.",event.getUei().matches("^uei.opennms.org/vendor/IBM/EIF/EIF_TEST_EVENT_TYPE_\\w$"));
            System.out.println("Checking value of parm situation_name: "+event.getParm("situation_name").getValue().getContent());
            assertEquals("DummyMonitoringSituation",event.getParm("situation_name").getValue().getContent());
        }
    }

    @Test
    public void EifTranslatorSemicolonInSlotTest() {
        String incomingEif = ".<START>>.............................EIF_TEST_EVENT_TYPE_A;cms_hostname='hubtems01';cms_port='3661';"
                +"integration_type='U';master_reset_flag='';appl_label='';situation_name='Situation 01';"
                +"situation_type='S';situation_origin='';situation_time='07/28/2016 12:19:11.000';situation_status='P';"
                +"situation_thrunode='REMOTE_teps_host';situation_fullname='Situation 01';situation_displayitem='';"
                +"source='EIF_TEST';sub_source='';hostname='';origin='';adapter_host='';date='07/28/2016';"
                +"severity='CRITICAL';IncidentSupportTeam='Server Support Testing';"
                +"semicolon_test='this is a test; of semicolons in; slot values';"
                +"onClose_msg='Event closed. OpenNMS EIF Testing.';onClose_severity='WARNING';send_delay='6';"
                +"msg='This is a test of EIF for OpenNMS';situation_eventdata='~';END";
        Event e = translateEifToOpenNMS(new StringBuilder(incomingEif)).get(0);
        assertEquals("uei.opennms.org/vendor/IBM/EIF/EIF_TEST_EVENT_TYPE_A",e.getUei());
        assertEquals("Situation 01",e.getParm("situation_name").getValue().getContent());
        assertEquals("~",e.getParm("situation_eventdata").getValue().getContent());
    }
}
