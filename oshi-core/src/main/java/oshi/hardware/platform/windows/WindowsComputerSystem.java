/**
 * Oshi (https://github.com/oshi/oshi)
 *
 * Copyright (c) 2010 - 2018 The Oshi Project Team
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Maintainers:
 * dblock[at]dblock[dot]org
 * widdis[at]gmail[dot]com
 * enrico.bianchi[at]gmail[dot]com
 *
 * Contributors:
 * https://github.com/oshi/oshi/graphs/contributors
 */
package oshi.hardware.platform.windows;

import oshi.hardware.common.AbstractComputerSystem;
import oshi.jna.platform.windows.WbemcliUtil;
import oshi.jna.platform.windows.WbemcliUtil.WmiQuery;
import oshi.jna.platform.windows.WbemcliUtil.WmiResult;
import oshi.util.platform.windows.WmiUtil;

/**
 * Hardware data obtained from WMI
 *
 * @author SchiTho1 [at] Securiton AG
 * @author widdis [at] gmail [dot] com
 */
final class WindowsComputerSystem extends AbstractComputerSystem {

    private static final long serialVersionUID = 1L;

    enum ComputerSystemProperty {
        MANUFACTURER, MODEL;
    }

    enum BiosProperty {
        SERIALNUMBER;
    }

    enum ComputerSystemProductProperty {
        IDENTIFYINGNUMBER;
    }

    private static final WmiQuery<ComputerSystemProductProperty> IDENTIFYINGNUMBER_QUERY = WbemcliUtil
            .createQuery("Win32_ComputerSystemProduct", ComputerSystemProductProperty.class);

    private String systemSerialNumber = "";

    WindowsComputerSystem() {
        init();
    }

    private void init() {
        WmiQuery<ComputerSystemProperty> computerSystemQuery = WbemcliUtil.createQuery("Win32_ComputerSystem",
                ComputerSystemProperty.class);
        WmiResult<ComputerSystemProperty> win32ComputerSystem = WmiUtil.queryWMI(computerSystemQuery);
        if (win32ComputerSystem.getResultCount() > 0) {
            setManufacturer(win32ComputerSystem.getString(ComputerSystemProperty.MANUFACTURER, 0));
            setModel(win32ComputerSystem.getString(ComputerSystemProperty.MODEL, 0));
        }

        setSerialNumber(getSystemSerialNumber());
        setFirmware(new WindowsFirmware());
        setBaseboard(new WindowsBaseboard());
    }

    private String getSystemSerialNumber() {
        if (!"".equals(this.systemSerialNumber)) {
            return this.systemSerialNumber;
        }
        // This should always work
        WmiQuery<BiosProperty> serialNumberQuery = WbemcliUtil.createQuery("Win32_BIOS where PrimaryBIOS=true",
                BiosProperty.class);
        WmiResult<BiosProperty> serialNumber = WmiUtil.queryWMI(serialNumberQuery);
        if (serialNumber.getResultCount() > 0) {
            this.systemSerialNumber = serialNumber.getString(BiosProperty.SERIALNUMBER, 0);
        }
        // If the above doesn't work, this might
        if (!"".equals(this.systemSerialNumber)) {
            WmiResult<ComputerSystemProductProperty> identifyingNumber = WmiUtil.queryWMI(IDENTIFYINGNUMBER_QUERY);
            if (identifyingNumber.getResultCount() > 0) {
                this.systemSerialNumber = identifyingNumber.getString(ComputerSystemProductProperty.IDENTIFYINGNUMBER, 0);
            }
        }
        // Nothing worked. Default.
        if (!"".equals(this.systemSerialNumber)) {
            this.systemSerialNumber = "unknown";
        }
        return this.systemSerialNumber;
    }
}
