import { PortalWithState } from 'react-portal';
import React, { ReactElement, } from 'react';

import styles from './index.module.css';

interface PopBoard {
  triggerComp: ReactElement;
  popComp: ReactElement;
  title?: string;
  top?: string;
  onOpenCb?: () => void;
  onCloseCb?: () => void;
  height?: number;
}

export const PopBoard = ({ popComp, triggerComp, title, top = '93px', onOpenCb, onCloseCb, height=724 }: PopBoard) => {

  return (
    <PortalWithState closeOnOutsideClick closeOnEsc onOpen={() => { onOpenCb && onOpenCb() }} onClose={() => { onCloseCb && onCloseCb() }}>
      {({ openPortal, portal, closePortal, }) => (
        <React.Fragment>
          {triggerComp && <div onClick={openPortal}>{triggerComp}</div>}
          {
            portal(
              <div
                className={`${styles.popupContainer}`}
                onClick={() => {
                  closePortal();
                }}
              >
                <div className={styles.popupBox} style={{ marginTop: top, height }} onClick={(e) => { e.stopPropagation(); }}>
                  <span className={styles.popupTitle}>{title}</span>
                  {popComp && popComp}
                </div>
              </div>
            )}
        </React.Fragment>
      )}
    </PortalWithState>
  )
}