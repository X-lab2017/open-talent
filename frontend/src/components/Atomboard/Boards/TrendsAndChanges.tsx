import newTag from './new.svg';
import increaseTag from './increase.svg';
import decreaseTag from './decrease.svg';

import styles from './index.module.css';

interface OpenRankProps {
  detail: number | '-';
}

export const TrendsAndChanges = ({ detail }: OpenRankProps) => {
  if (detail === '-') {
    return (
      <div className={styles.openrankContainer}>
        <img className={styles.tag} src={newTag} alt='new' />
      </div>
    );
  }
  return (
    <div className={styles.openrankContainer}>
      <img style={{marginRight:4}} src={detail > 0 ? increaseTag : decreaseTag} alt='increase/decrease' />
      <span>{detail+'%'}</span>
    </div>
  );
}